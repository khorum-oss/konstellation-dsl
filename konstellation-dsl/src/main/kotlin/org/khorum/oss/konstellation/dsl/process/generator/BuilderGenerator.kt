package org.khorum.oss.konstellation.dsl.process.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.joinToCode
import com.squareup.kotlinpoet.ksp.writeTo
import org.khorum.oss.konstellation.dsl.builder.AnnotationDecorator
import org.khorum.oss.konstellation.dsl.builder.kotlinPoet
import org.khorum.oss.konstellation.dsl.domain.BuilderConfig
import org.khorum.oss.konstellation.dsl.domain.DomainConfig
import org.khorum.oss.konstellation.dsl.domain.InjectedMethod
import org.khorum.oss.konstellation.dsl.process.DslFileWriter
import org.khorum.oss.konstellation.dsl.process.propSchema.DefaultPropertySchemaService
import org.khorum.oss.konstellation.dsl.schema.DslPropSchema
import org.khorum.oss.konstellation.dsl.utils.AnnotationLookup
import org.khorum.oss.konstellation.dsl.utils.VLoggable
import org.khorum.oss.konstellation.dsl.utils.cleanDocString
import org.khorum.oss.konstellation.dsl.utils.isGroupDsl
import org.khorum.oss.konstellation.dsl.utils.hasMapDsl

/** * Interface for generating DSL builders.
 * This interface defines the contract for generating DSL builder files based on domain configurations.
 */
interface BuilderGenerator : DslFileWriter, VLoggable {
    override fun logId(): String? = BuilderGenerator::class.simpleName

    /**
     * Generates the DSL builder files for the given domain.
     *
     * @param codeGenerator The KSP CodeGenerator instance used to write the generated files.
     * @param domain The KSClassDeclaration representing the domain for which the builder is generated.
     * @param builderConfig The configuration for the DSL builder.
     * @param singleEntryTransformByClassName A map of class names to their
     *                                        corresponding KSClassDeclaration for single entry transformations.
     */
    fun generate(
        codeGenerator: CodeGenerator,
        domain: KSClassDeclaration,
        builderConfig: BuilderConfig,
        singleEntryTransformByClassName: Map<String, KSClassDeclaration>,
        debug: Boolean
    )
}

/**
 * Default implementation of [BuilderGenerator].
 * This class provides the default behavior for generating DSL builders.
 * @property parameterService The service used to retrieve property schemas from the domain.
 * @property annotationDecorator The decorator for handling annotations in the DSL builder.
 * @property mapGroupGenerator The generator for map groups in the DSL builder.
 * @property listGroupGenerator The generator for list groups in the DSL builder.
 */
class DefaultBuilderGenerator(
    val parameterService: DefaultPropertySchemaService = DefaultPropertySchemaService(),
    val annotationDecorator: AnnotationDecorator = AnnotationDecorator(),
    val mapGroupGenerator: MapGroupGenerator = MapGroupGenerator(),
    val listGroupGenerator: ListGroupGenerator = ListGroupGenerator(),
) : BuilderGenerator {
    override fun generate(
        codeGenerator: CodeGenerator,
        domain: KSClassDeclaration,
        builderConfig: BuilderConfig,
        singleEntryTransformByClassName: Map<String, KSClassDeclaration>,
        debug: Boolean
    ) {
        val domainConfig = DomainConfig(
            builderConfig,
            singleEntryTransformByClassName,
            domain,
            debug
        )
        generateFilesForDsl(domainConfig, codeGenerator)
    }

    /**
     * Generates the DSL builder files for the given domain configuration.
     *
     * @param domainConfig The configuration for the domain, including package name, class names, and builder details.
     * @param codeGenerator The KSP CodeGenerator instance used to write the generated files.
     */
    private fun generateFilesForDsl(
        domainConfig: DomainConfig,
        codeGenerator: CodeGenerator
    ) = debugLog(domainConfig) {
        val schemas: List<DslPropSchema> = parameterService.getParamsFromDomain(domainConfig)

        val builderContent: TypeSpec = generateBuilderFileContent(domainConfig, schemas)

        val builderScopeTypeAlias: String = domainConfig.builderName

        val typeAliasNames: MutableList<String> = mutableListOf(builderScopeTypeAlias)

        val hasGroup = domainConfig.domain.isGroupDsl()
        val hasMapGroup = domainConfig.domain.hasMapDsl()

        if (hasGroup) typeAliasNames.add("${builderScopeTypeAlias}.Group")
        if (hasMapGroup) typeAliasNames.add("${builderScopeTypeAlias}.MapGroup")

        val typeAliases: List<TypeAliasSpec> = generateTypeAliases(typeAliasNames, domainConfig)

        val fileSpec = createFileSpec(schemas, domainConfig, typeAliases, builderContent)

        fileSpec.writeTo(codeGenerator, domainConfig.dependencies)
    }

    private fun debugLog(domainConfig: DomainConfig, runnable: () -> Unit) {
        VLoggable.setGlobalDebug(domainConfig.debug)
        logger.debug("-- generating builder --", tier = 0)
        logger.debug("+++ DOMAIN: ${domainConfig.domainClassName}  +++")
        logger.debug("package: ${domainConfig.packageName}", tier = 1, branch = true)
        logger.debug("type: ${domainConfig.typeName}", tier = 1, branch = true)
        logger.debug("builder: ${domainConfig.builderName}", tier = 1, branch = true)

        runnable()

        logger.debug("file written: ${domainConfig.fileClassName}", tier = 1)

        VLoggable.resetGlobalDebug()
    }

    /**
     * Generates the content of the DSL builder file.
     *
     * @param domainConfig The configuration for the domain, including package name, class names, and builder details.
     * @param params The list of property schemas to be included in the builder.
     * @return A TypeSpec representing the DSL builder interface.
     */
    private fun generateBuilderFileContent(
        domainConfig: DomainConfig,
        params: List<DslPropSchema>
    ): TypeSpec = kotlinPoet {
        val domainClassName = domainConfig.domainClassName

        // Check for @DslDescription on the domain class for builder KDoc
        val descriptionAnnotation = AnnotationLookup.findAnnotationByName(
            domainConfig.domain.annotations, "DslDescription"
        )
        val classDescription = if (descriptionAnnotation != null) {
            val desc = AnnotationLookup.findArgumentValue<String>(descriptionAnnotation, "value")
            if (desc != null && desc.isNotBlank()) desc else null
        } else null

        type {
            annotations {
                annotationDecorator
                    .createDslMarkerIfAvailable(domainConfig.builderConfig.dslMarkerClass)
                    ?.also { annotation(it) }
            }
            public()
            name = domainConfig.builderName

            // Add KDoc from @DslDescription on the class, falling back to source KDoc
            val effectiveClassDoc = classDescription ?: cleanDocString(domainConfig.domain.docString)
            effectiveClassDoc?.let { kdoc(it) }

            superInterface(domainConfig.parameterizedDslBuilder)
            logger.debug("DSL Builder Interface added", tier = 1, branch = true)
            logger.debug("Properties added", tier = 1)

            properties {
                params.addForEach(DslPropSchema::toPropertySpec)
            }

            functions {
                params.addForEach(DslPropSchema::allAccessors)

                // @InjectDslMethod: copy annotated functions into the builder
                val injectedFunSpecs = buildInjectedMethods(domainConfig, params)
                addAll(injectedFunSpecs)

                add {
                    override()
                    funName = "build"
                    returns = domainClassName

                    statements {
                        // Emit all build-time statements (transformations, validations)
                        val allBuildStatements = params.flatMap { it.buildStatements() }
                        for (statement in allBuildStatements) {
                            addLine(statement)
                        }

                        val constructorParams = params
                            .map { CodeBlock.of("%N = %L", it.propName, it.propertyValueReturn()) }
                        if (constructorParams.isEmpty()) {
                            addLine("return %T()", domainClassName)
                        } else {
                            val argumentsBlock = constructorParams.joinToCode(
                                separator = ",\n    ",
                                prefix = "\n    ",
                                suffix = "\n"
                            )
                            addLine("return %T(%L)", domainClassName, argumentsBlock)
                        }
                    }
                }
            }

            listGroupGenerator.generate(this, domainConfig)
            mapGroupGenerator.generate(this, domainConfig)
        }
    }

    private fun generateTypeAliases(
        typeAliasNames: List<String>,
        domainConfig: DomainConfig
    ): List<TypeAliasSpec> {
        return typeAliasNames
            .onEach {
                logger.debug("typeAlias added: $it", tier = 1, branch = true)
            }
            .map { aliasBaseClassName ->
                kotlinPoet {
                    param {
                        name = aliasBaseClassName
                            .replace(".", "")
                            .let { "${it}Scope" }
                        lambdaType {
                            val hasMap = aliasBaseClassName.contains("MapGroup")
                            val originalClassName = ClassName(domainConfig.packageName, aliasBaseClassName)
                            receiver = if (hasMap)
                                originalClassName.parameterizedBy(TypeVariableName("K"))
                            else
                                originalClassName
                        }
                    }
                }
            }
            .map {
                val hasMap = it.type.toString().contains("MapGroup")
                kotlinPoet {
                    typeAlias {
                        name = it.name
                        type = it.type
                        if (hasMap) typeVariables(TypeVariableName("K"))
                    }
                }
            }
    }

    @Suppress("SpreadOperator")
    private fun createFileSpec(
        schemas: List<DslPropSchema>,
        domainConfig: DomainConfig,
        typeAliases: List<TypeAliasSpec>,
        builderContent: TypeSpec
    ): FileSpec {
        val hasRequireNotNull = schemas.any { param -> !param.nullableAssignment && param.verifyNotNull }
        val hasCollectionRequireNotEmpty = schemas.any { param ->
            !param.nullableAssignment && param.verifyNotEmpty && param.isCollection()
        }
        val hasMapRequireNotEmpty = schemas.any { param ->
            !param.nullableAssignment && param.verifyNotEmpty && param.isMap()
        }
        logger.debug("requiresNotNull: $hasRequireNotNull", tier = 1, branch = true)
        logger.debug("requireCollectionNotEmpty: $hasCollectionRequireNotEmpty", tier = 1, branch = true)
        logger.debug("requireMapNotEmpty: $hasMapRequireNotEmpty", tier = 1, branch = true)

        val defaultValueImports: Set<String> = schemas
            .mapNotNull { it.defaultValue?.importString() }
            .toSet()

        logger.debug("defaultValueImports: $defaultValueImports", tier = 1, branch = true)

        return kotlinPoet {
            file {
                val needsDslValidation = hasRequireNotNull || hasCollectionRequireNotEmpty || hasMapRequireNotEmpty
                addImportIf(needsDslValidation, META_DSL_PACKAGE, "DslValidation")
                defaultValueImports.forEach {
                    addImport(it)
                }
                className = domainConfig.fileClassName
                typeAliases(*typeAliases.toTypedArray())
                types(builderContent)
            }
        }
    }

    /**
     * Validates and builds [FunSpec] objects for `@InjectDslMethod` annotated functions.
     * Throws an error if the function body references properties not present in the builder.
     */
    private fun buildInjectedMethods(
        domainConfig: DomainConfig,
        params: List<DslPropSchema>
    ): List<FunSpec> {
        val injectedMethods = domainConfig.injectedMethods
        if (injectedMethods.isEmpty()) return emptyList()

        val builderPropNames = params.map { it.propName }.toSet()

        return injectedMethods.map { method ->
            validateBackingProperties(method, builderPropNames, domainConfig)

            val funBuilder = FunSpec.builder(method.name)
                .returns(method.returnType)

            for (param in method.parameters) {
                funBuilder.addParameter(param.name, param.type)
            }

            val body = method.body
            if (body.startsWith("{")) {
                // Block body — strip outer braces and add as code block
                val inner = body.removeSurrounding("{", "}").trim()
                funBuilder.addCode(CodeBlock.of("%L", inner))
            } else {
                // Expression body
                funBuilder.addCode(CodeBlock.of("return %L", body))
            }

            logger.debug("Injected method '${method.name}' added to builder", tier = 2, branch = true)
            funBuilder.build()
        }
    }

    private fun validateBackingProperties(
        method: InjectedMethod,
        builderPropNames: Set<String>,
        domainConfig: DomainConfig
    ) {
        val body = method.body
        // Simple token-based check: find identifiers in the body that could be property references.
        // We look for property names used as bare identifiers (not after a dot that isn't 'this.').
        val allPropertyNames = domainConfig.domain.getAllProperties()
            .map { it.simpleName.asString() }
            .toSet()

        // Find property references in the body that exist in the domain but not in the builder
        val missingProps = allPropertyNames
            .filter { propName -> body.contains(propName) && propName !in builderPropNames }

        for (missing in missingProps) {
            logger.warn(
                "@InjectDslMethod '${method.name}' references property '$missing' " +
                    "which is not available in the builder (it may be @TransientDsl). " +
                    "This will cause a compile error in the generated code."
            )
        }
    }

    companion object {
        const val META_DSL_PACKAGE = "org.khorum.oss.konstellation.metaDsl"
    }
}

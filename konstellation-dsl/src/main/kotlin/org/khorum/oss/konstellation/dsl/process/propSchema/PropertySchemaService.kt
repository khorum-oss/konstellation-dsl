package org.khorum.oss.konstellation.dsl.process.propSchema

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import org.khorum.oss.konstellation.dsl.domain.DefaultDomainProperty
import org.khorum.oss.konstellation.dsl.domain.BooleanAccessorConfig
import org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue
import org.khorum.oss.konstellation.dsl.domain.DomainConfig
import org.khorum.oss.konstellation.dsl.domain.DomainProperty
import org.khorum.oss.konstellation.dsl.schema.DslPropSchema
import org.khorum.oss.konstellation.dsl.utils.AnnotationLookup
import org.khorum.oss.konstellation.dsl.utils.Colors
import org.khorum.oss.konstellation.dsl.utils.VLoggable
import org.khorum.oss.konstellation.dsl.utils.cleanDocString
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.DefaultEnum
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.DefaultValue
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.DefaultState
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.DefaultStateType

/**
 * Service to handle the conversion of domain properties into DSL property schemas.
 */
interface PropertySchemaService<FACTORY_ADAPTER : PropertySchemaFactoryAdapter, PROP_ADAPTER : DomainProperty> :
    VLoggable {
    override fun logId(): String? = PropertySchemaService::class.simpleName

    fun getParamsFromDomain(domainConfig: DomainConfig): List<DslPropSchema>
}

/**
 * Default implementation of [PropertySchemaService] that
 * uses [DefaultPropertySchemaFactory] to create property schemas.
 * It converts domain properties into DSL property schemas.
 */
class DefaultPropertySchemaService(
    private val propertySchemaFactory: DefaultPropertySchemaFactory = DefaultPropertySchemaFactory(),
    private val annotationExtractor: PropertyAnnotationExtractor = DefaultPropertyAnnotationExtractor()
) : PropertySchemaService<DefaultPropertySchemaFactoryAdapter, DefaultDomainProperty> {
    override fun getParamsFromDomain(domainConfig: DomainConfig): List<DslPropSchema> {
        val domain = domainConfig.domain
        val allProps = domain.getAllProperties().toList()

        // Materialize annotations eagerly for each property to avoid KSP sequence re-consumption issues.
        // KSP annotation sequences may be single-use; materializing them once ensures all extraction
        // methods see the full annotation set.
        val propsWithAnnotations = allProps.map { prop ->
            prop to prop.annotations.toList()
        }

        // Filter out @TransientDsl properties
        val nonTransientProps = propsWithAnnotations.filter { (prop, annotations) ->
            val metadata = annotationExtractor.extract(annotations.asSequence())
            if (metadata.isTransient) {
                logger.debug(
                    "Property '${prop.simpleName.asString()}' is ${Colors.yellow("@TransientDsl")}" +
                        (metadata.transientReason?.let { " (reason: $it)" } ?: "") +
                        " — skipping",
                    tier = 2, branch = true
                )
                false
            } else {
                true
            }
        }

        val lastIndex = nonTransientProps.size - 1

        return nonTransientProps
            .mapIndexed { i, (prop, annotations) ->
                val defaultValue = resolveDefaultValue(prop, annotations)
                val annotationMetadata = annotationExtractor.extract(annotations.asSequence()).let { meta ->
                    val propDocString = cleanDocString(prop.docString)
                    if (propDocString != null) meta.copy(docString = propDocString) else meta
                }

                defaultValue?.let {
                    logger.debug(
                        "Property '${prop.simpleName.asString()}' has default: $it",
                        tier = 2, branch = true
                    )
                }

                DefaultDomainProperty(
                    i, lastIndex,
                    prop,
                    domainConfig.singleEntryTransformByClassName,
                    defaultValue,
                    annotationMetadata = annotationMetadata
                )
            }
            .map(propertySchemaFactory::createPropertySchemaFactoryAdapter)
            .map(propertySchemaFactory::determinePropertySchema)
            .toList()
    }

    /**
     * Resolves the default value for a property, checking `@DefaultState`, `@DefaultEnum`, and `@DefaultValue`.
     * Emits a warning if multiple are present (mutual exclusivity).
     * Precedence: @DefaultState > @DefaultEnum > @DefaultValue.
     */
    private fun resolveDefaultValue(
        prop: KSPropertyDeclaration,
        annotations: List<KSAnnotation>
    ): DefaultPropertyValue? {
        val fromDefaultValue = extractDefaultPropertyValue(prop, annotations)
        val fromDefaultState = extractDefaultState(prop, annotations)
        val fromDefaultEnum = extractDefaultEnum(prop, annotations)
        val propNameStr = prop.simpleName.asString()

        val presentAnnotations = listOfNotNull(
            fromDefaultState?.let { "@DefaultState" },
            fromDefaultEnum?.let { "@DefaultEnum" },
            fromDefaultValue?.let { "@DefaultValue" }
        )

        if (presentAnnotations.size > 1) {
            val names = presentAnnotations.joinToString(" and ") { Colors.yellow(it) }
            val winner = presentAnnotations.first()
            logger.warn(
                "Property '$propNameStr' has both $names. " +
                    "These are mutually exclusive — $winner will take precedence."
            )
        }

        return fromDefaultState ?: fromDefaultEnum ?: fromDefaultValue
    }

    /**
     * If the property has @DefaultState(type), return a [DefaultPropertyValue] with the literal code snippet.
     *
     * The annotation's `type` parameter is a [DefaultStateType] enum. In KSP, enum annotation values
     * are represented as [KSClassDeclaration] (enum entry), not [KSType]. We extract the entry's
     * simple name and map it to the corresponding [DefaultStateType.codeSnippet].
     */
    private fun extractDefaultState(
        prop: KSPropertyDeclaration,
        annotations: List<KSAnnotation>
    ): DefaultPropertyValue? {
        val defaultStateQualName = DefaultState::class.qualifiedName

        // Direct @DefaultState on the property
        val ann: KSAnnotation? = annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == defaultStateQualName
        }

        val propName = prop.simpleName.asString()

        // If found directly, resolve via the annotation's type argument
        if (ann != null) {
            return resolveDefaultStateType(ann, propName)?.let(::buildDefaultStateValue)
        }

        // Otherwise, check for shorthand annotations (e.g. @DefaultEmptyString → EMPTY_STRING).
        // These are meta-annotated with @DefaultState in source, but since @DefaultState has SOURCE
        // retention, the meta-annotation is stripped from the compiled JAR. We resolve by qualified name.
        var matchedAnnotation: KSAnnotation? = null
        val stateType = annotations.firstNotNullOfOrNull { outerAnn ->
            val qualName = outerAnn.annotationType.resolve().declaration.qualifiedName?.asString()
            qualName?.let { SHORTHAND_ANNOTATION_MAP[it] }?.also { matchedAnnotation = outerAnn }
        } ?: return null

        logger.debug(
            "Property '$propName' has shorthand ${Colors.yellow("@Default*")} " +
                "→ ${stateType.name}: ${stateType.codeSnippet}",
            tier = 2
        )

        // For @DefaultTrue / @DefaultFalse, extract boolean accessor template parameters
        val booleanAccessorConfig = if (stateType == DefaultStateType.TRUE || stateType == DefaultStateType.FALSE) {
            extractBooleanAccessorConfig(matchedAnnotation!!)
        } else null

        return buildDefaultStateValue(stateType, booleanAccessorConfig)
    }

    private fun buildDefaultStateValue(
        stateType: DefaultStateType,
        booleanAccessorConfig: BooleanAccessorConfig? = null
    ): DefaultPropertyValue {
        return DefaultPropertyValue(
            rawValue = stateType.codeSnippet,
            codeBlock = CodeBlock.of("%L", stateType.codeSnippet),
            packageName = "",
            className = "",
            booleanAccessorConfig = booleanAccessorConfig
        )
    }

    /**
     * Extracts boolean accessor configuration from `@DefaultTrue` / `@DefaultFalse` annotations.
     * These annotations may carry `validFunctionName`, `validTemplate`, `negationFunctionName`,
     * and `negationTemplate` parameters for generating paired valid/negation accessor functions.
     */
    private fun extractBooleanAccessorConfig(ann: KSAnnotation): BooleanAccessorConfig? {
        val validFunctionName = AnnotationLookup.findArgumentValue<String>(ann, "validFunctionName")
            .takeUnlessBlank()
        val validTemplateArg = AnnotationLookup.findArgument(ann, "validTemplate")
        val validTemplate = if (validTemplateArg != null) resolveEnumEntryName(validTemplateArg.value) else null

        val negationFunctionName = AnnotationLookup.findArgumentValue<String>(ann, "negationFunctionName")
            .takeUnlessBlank()
        val negationTemplateArg = AnnotationLookup.findArgument(ann, "negationTemplate")
        val negationTemplate = if (negationTemplateArg != null) {
            resolveEnumEntryName(negationTemplateArg.value)
        } else null

        // If no template parameters are present, no config needed (backward compatibility)
        val allParams = listOf(validFunctionName, validTemplate, negationFunctionName, negationTemplate)
        if (allParams.all { it == null }) {
            return null
        }

        // If negation is SELF, blank out valid unless explicitly set
        val negationIsSelf = negationTemplate == "SELF"
        val noValidOverride = validTemplate == null && validFunctionName == null
        val effectiveValidTemplate = if (negationIsSelf && noValidOverride) {
            "NONE"
        } else {
            validTemplate
        }

        return BooleanAccessorConfig(
            validFunctionName = validFunctionName,
            validTemplate = effectiveValidTemplate,
            negationFunctionName = negationFunctionName,
            negationTemplate = negationTemplate
        )
    }

    /**
     * If the property has @DefaultEnum("ENTRY_NAME"), return a [DefaultPropertyValue] that
     * initializes the property to the specified enum entry.
     *
     * When `packageName` and `className` are empty, the enum class is inferred from the
     * property's declared type.
     */
    private fun extractDefaultEnum(
        prop: KSPropertyDeclaration,
        annotations: List<KSAnnotation>
    ): DefaultPropertyValue? {
        val ann: KSAnnotation = annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == DefaultEnum::class.qualifiedName
        } ?: return null

        val value = AnnotationLookup.findArgumentValue<String>(ann, DefaultEnum::value.name)
        if (value.isNullOrBlank()) {
            logger.warn("@DefaultEnum on '${prop.simpleName.asString()}' has no value — ignoring")
            return null
        }

        var packageName = AnnotationLookup.findArgumentValue<Any>(ann, DefaultEnum::packageName.name)?.toString() ?: ""
        var className = AnnotationLookup.findArgumentValue<Any>(ann, DefaultEnum::className.name)?.toString() ?: ""

        // Auto-infer enum class from property type when packageName/className are empty
        if (packageName.isEmpty() && className.isEmpty()) {
            val propTypeDecl = prop.type.resolve().declaration
            val qualifiedName = propTypeDecl.qualifiedName?.asString()
            val propPackage = propTypeDecl.packageName.asString()

            if (qualifiedName != null && propPackage.isNotEmpty()) {
                packageName = propPackage
                // className is the part after the package: e.g. "Passenger.Rank" from "com.example.Passenger.Rank"
                className = qualifiedName.removePrefix("$propPackage.")
            }
        }

        val propName = prop.simpleName.asString()
        val enumReference = "$className.$value"

        logger.debug(
            "Property '$propName' has ${Colors.yellow("@DefaultEnum")} → $enumReference",
            tier = 2
        )

        return DefaultPropertyValue(
            rawValue = value,
            codeBlock = CodeBlock.of("%L", enumReference),
            packageName = packageName,
            className = className
        )
    }

    /**
     * Resolves the [DefaultStateType] from a @DefaultState annotation's `type` argument.
     * KSP represents enum annotation values as [KSClassDeclaration] (enum entry impl).
     */
    private fun resolveDefaultStateType(ann: KSAnnotation, propName: String): DefaultStateType? {
        val typeArg = AnnotationLookup.findArgument(ann, DefaultState::type.name)?.value

        val entryName: String = resolveEnumEntryName(typeArg) ?: run {
            logger.warn("@DefaultState on '$propName' has no type value — ignoring")
            return null
        }

        return DefaultStateType.entries.firstOrNull { it.name == entryName }.also {
            if (it == null) {
                logger.warn("@DefaultState on '$propName' has unknown type '$entryName' — ignoring")
            }
        }
    }

    private fun resolveEnumEntryName(typeArg: Any?): String? = when (typeArg) {
        is KSClassDeclaration -> typeArg.simpleName.asString()
        is KSType -> typeArg.declaration.simpleName.asString()
        else -> typeArg?.toString()?.substringAfterLast(".")
    }

    /**
     * If the property has @DefaultValue("..."), return a parsed [DefaultPropertyValue], else null.
     */
    private fun extractDefaultPropertyValue(
        prop: KSPropertyDeclaration,
        annotations: List<KSAnnotation>
    ): DefaultPropertyValue? {
        val ann: KSAnnotation = annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == DefaultValue::class.qualifiedName
        } ?: return null

        return buildDefaultPropertyValue(prop, ann)
    }

    private fun buildDefaultPropertyValue(
        prop: KSPropertyDeclaration,
        ann: KSAnnotation
    ): DefaultPropertyValue? {
        val raw = AnnotationLookup.findArgumentValue<String>(ann, DefaultValue::value.name)
        val packageName = AnnotationLookup.findArgumentValue<Any>(ann, DefaultValue::packageName.name)?.toString()
        val className = AnnotationLookup.findArgumentValue<Any>(ann, DefaultValue::className.name)?.toString()
        val inferType = AnnotationLookup.findArgumentValue<Boolean>(ann, "inferType") ?: true

        if (raw == null || packageName == null || className == null) return null

        logger.debug("Raw default value from annotation: '$raw'", tier = 2)
        logger.debug("Class reference: $packageName.$className", tier = 2)

        val isLiteral = if (inferType && className.isEmpty() && packageName.isEmpty()) {
            val propTypeName = prop.type.resolve().declaration.qualifiedName?.asString()
            propTypeName in PRIMITIVE_TYPE_NAMES
        } else {
            false
        }

        val isStringClass = !isLiteral && (
            className == "String" || (className.isEmpty() && packageName.isEmpty())
        )

        logger.debug("Is String class: $isStringClass, is literal: $isLiteral", tier = 2)
        val template = if (isStringClass) "%S" else "%L"
        val cb = CodeBlock.of(template, raw)
        logger.debug("CodeBlock for default value: $cb", tier = 2)

        return DefaultPropertyValue(rawValue = raw, codeBlock = cb, packageName, className)
    }

    companion object {
        private val PRIMITIVE_TYPE_NAMES = setOf(
            "kotlin.Int", "kotlin.Long", "kotlin.Short", "kotlin.Byte",
            "kotlin.Float", "kotlin.Double", "kotlin.Boolean", "kotlin.Char"
        )

        private const val SHORTHAND_PKG =
            "org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard"

        /**
         * Maps shorthand default-state annotation qualified names to their [DefaultStateType].
         * These annotations are meta-annotated with @DefaultState in source, but since @DefaultState
         * has SOURCE retention, the meta-annotation is not available in compiled bytecode.
         */
        private val SHORTHAND_ANNOTATION_MAP: Map<String, DefaultStateType> = DefaultStateType.entries
            .associateBy { entry ->
                val annotationName = entry.name
                    .lowercase()
                    .split("_")
                    .joinToString("") { part ->
                        part.first().uppercase() + part.substring(1)
                    }.let { "Default$it" }
                "$SHORTHAND_PKG.$annotationName"
            }
    }

}

private fun String?.takeUnlessBlank(): String? = if (this != null && this.isNotBlank()) this else null

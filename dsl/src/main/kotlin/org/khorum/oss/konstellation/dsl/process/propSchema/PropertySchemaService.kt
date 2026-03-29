package org.khorum.oss.konstellation.dsl.process.propSchema

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import org.khorum.oss.konstellation.dsl.domain.DefaultDomainProperty
import org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue
import org.khorum.oss.konstellation.dsl.domain.DomainConfig
import org.khorum.oss.konstellation.dsl.domain.DomainProperty
import org.khorum.oss.konstellation.dsl.schema.DslPropSchema
import org.khorum.oss.konstellation.dsl.utils.Colors
import org.khorum.oss.konstellation.dsl.utils.VLoggable
import org.khorum.oss.konstellation.metaDsl.annotation.DefaultState
import org.khorum.oss.konstellation.metaDsl.annotation.DefaultStateType
import org.khorum.oss.konstellation.metaDsl.annotation.DefaultValue

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
                val annotationMetadata = annotationExtractor.extract(annotations.asSequence())

                if (defaultValue != null) logger.debug(
                    "Property '${prop.simpleName.asString()}' has default: $defaultValue",
                    tier = 2, branch = true
                )

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
     * Resolves the default value for a property, checking both `@DefaultState` and `@DefaultValue`.
     * Emits a warning if both are present (mutual exclusivity) — `@DefaultState` takes precedence.
     */
    private fun resolveDefaultValue(
        prop: KSPropertyDeclaration,
        annotations: List<KSAnnotation>
    ): DefaultPropertyValue? {
        val fromDefaultValue = extractDefaultPropertyValue(prop, annotations)
        val fromDefaultState = extractDefaultState(prop, annotations)
        val propNameStr = prop.simpleName.asString()

        if (fromDefaultValue != null && fromDefaultState != null) {
            logger.warn(
                "Property '$propNameStr' has both ${Colors.yellow("@DefaultValue")} and " +
                    "${Colors.yellow("@DefaultState")}. These are mutually exclusive — " +
                    "@DefaultState will take precedence."
            )
            return fromDefaultState
        }

        return fromDefaultState ?: fromDefaultValue
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
        val ann: KSAnnotation = annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == DefaultState::class.qualifiedName
        } ?: return null

        val propName = prop.simpleName.asString()

        return resolveDefaultStateType(ann, propName)?.let { stateType ->
            val codeSnippet = stateType.codeSnippet
            logger.debug(
                "Property '$propName' has ${Colors.yellow("@DefaultState")}(${stateType.name}): $codeSnippet",
                tier = 2
            )
            DefaultPropertyValue(
                rawValue = codeSnippet,
                codeBlock = CodeBlock.of("%L", codeSnippet),
                packageName = "",
                className = ""
            )
        }
    }

    /**
     * Resolves the [DefaultStateType] from a @DefaultState annotation's `type` argument.
     * KSP represents enum annotation values as [KSClassDeclaration] (enum entry impl).
     */
    private fun resolveDefaultStateType(ann: KSAnnotation, propName: String): DefaultStateType? {
        val typeArg = ann.arguments
            .firstOrNull { it.name?.asString() == DefaultState::type.name }
            ?.value

        val entryName: String? = when (typeArg) {
            is KSClassDeclaration -> typeArg.simpleName.asString()
            is KSType -> typeArg.declaration.simpleName.asString()
            else -> typeArg?.toString()?.substringAfterLast(".")
        }

        if (entryName == null) {
            logger.warn("@DefaultState on '$propName' has no type value — ignoring")
            return null
        }

        return DefaultStateType.entries.firstOrNull { it.name == entryName }.also {
            if (it == null) {
                logger.warn("@DefaultState on '$propName' has unknown type '$entryName' — ignoring")
            }
        }
    }

    /**
     * If the property has @DefaultValue("..."), return a parsed [DefaultPropertyValue], else null.
     */
    private fun extractDefaultPropertyValue(
        prop: KSPropertyDeclaration,
        annotations: List<KSAnnotation>
    ): DefaultPropertyValue? {
        // find annotation
        val ann: KSAnnotation? = annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == DefaultValue::class.qualifiedName
        }

        // get the String argument
        val raw = ann?.arguments
            ?.firstOrNull { it.name?.asString() == DefaultValue::value.name }
            ?.value as? String

        if (raw != null) logger.debug("Raw default value from annotation: '$raw'", tier = 2)

        val packageName = ann?.arguments
            ?.firstOrNull { it.name?.asString() == DefaultValue::packageName.name }
            ?.value
            ?.toString()

        val className = ann?.arguments
            ?.firstOrNull { it.name?.asString() == DefaultValue::className.name }
            ?.value
            ?.toString()

        val inferType = ann?.arguments
            ?.firstOrNull { it.name?.asString() == "inferType" }
            ?.value as? Boolean ?: true

        if (raw == null || packageName == null || className == null) return null

        logger.debug("Class reference: $packageName.$className", tier = 2)

        // When inferType is true and no explicit className/packageName, check the property type
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
        val template = when {
            isStringClass -> "%S"
            else -> "%L"
        }
        val cb = CodeBlock.of(template, raw)
        logger.debug("CodeBlock for default value: $cb", tier = 2)

        return DefaultPropertyValue(rawValue = raw, codeBlock = cb, packageName, className)
    }

    companion object {
        private val PRIMITIVE_TYPE_NAMES = setOf(
            "kotlin.Int", "kotlin.Long", "kotlin.Short", "kotlin.Byte",
            "kotlin.Float", "kotlin.Double", "kotlin.Boolean", "kotlin.Char"
        )
    }

}

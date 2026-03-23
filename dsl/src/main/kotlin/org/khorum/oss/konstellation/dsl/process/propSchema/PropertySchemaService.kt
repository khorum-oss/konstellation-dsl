package org.khorum.oss.konstellation.dsl.process.propSchema

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.CodeBlock
import org.khorum.oss.konstellation.dsl.domain.DefaultDomainProperty
import org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue
import org.khorum.oss.konstellation.dsl.domain.DomainConfig
import org.khorum.oss.konstellation.dsl.domain.DomainProperty
import org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata
import org.khorum.oss.konstellation.dsl.schema.DslPropSchema
import org.khorum.oss.konstellation.dsl.utils.Colors
import org.khorum.oss.konstellation.dsl.utils.VLoggable
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

        // Filter out @TransientDsl properties
        val nonTransientProps = allProps.filter { prop ->
            val metadata = prop.extractAnnotationMetadata()
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
            .mapIndexed { i, prop ->
                val defaultValue = prop.extractDefaultPropertyValue()
                val annotationMetadata = prop.extractAnnotationMetadata()

                if (defaultValue != null) logger.debug(
                    "Property '${prop.simpleName.asString()}' has ${Colors.yellow("@DefaultValue")}: $defaultValue",
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
     * If the property has @DefaultValue("..."), return a parsed [DefaultPropertyValue], else null.
     */
    private fun KSPropertyDeclaration.extractDefaultPropertyValue(): DefaultPropertyValue? {
        // find annotation
        val ann: KSAnnotation? = annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == DefaultValue::class.qualifiedName
        }

        // get the String argument
        val raw = ann?.arguments
            ?.firstOrNull { it.name?.asString() == DefaultValue::value.name }
            ?.value as? String

        if (raw != null) logger.debug("Raw default value from annotation: ‘$raw’", tier = 2)

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
            val propTypeName = type.resolve().declaration.qualifiedName?.asString()
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

    /**
     * Extract metadata from new-style annotations (@TransientDsl, @DslDescription,
     * @DslAlias, @DeprecatedDsl, @ValidateDsl) on a property declaration.
     * Delegates to [PropertyAnnotationExtractor] for the actual extraction logic.
     */
    private fun KSPropertyDeclaration.extractAnnotationMetadata(): PropertyAnnotationMetadata {
        return annotationExtractor.extract(this.annotations)
    }
}

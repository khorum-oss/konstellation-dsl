package org.khorum.oss.konstellation.dsl.schema

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import org.khorum.oss.konstellation.dsl.builder.kotlinPoet
import org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue
import org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata

/**
 * Represents a property in a generated DSL builder.
 */
private const val DSL_VALIDATION_CLASS = "org.khorum.oss.konstellation.metaDsl.DslValidation"

interface DslPropSchema {
    val propName: String
    val functionName: String get() = propName
    val propTypeName: TypeName // This should be the type of the actual property in the domain object
    val nullableAssignment: Boolean get() = true
    val verifyNotNull: Boolean get() = true
    val verifyNotEmpty: Boolean get() = false
    val iterableType: IterableType? get() = null
    val accessModifier: KModifier get() = KModifier.PROTECTED
    val defaultValue: DefaultPropertyValue? get() = null
    val annotationMetadata: PropertyAnnotationMetadata get() = PropertyAnnotationMetadata()

    fun isCollection(): Boolean = iterableType == IterableType.COLLECTION
    fun isMap(): Boolean = iterableType == IterableType.MAP

    /**
     * Create the KotlinPoet [PropertySpec] representing this DSL property.
     */
    fun toPropertySpec(): PropertySpec = kotlinPoet {
        property {
            accessModifier(accessModifier)
            variable()
            name = propName
            type(propTypeName.copy(nullable = true))

            // Add KDoc from @DslDescription or source KDoc comment
            val desc = annotationMetadata.effectiveDescription
            if (desc != null) kdoc(desc)

            val codeBlock = defaultValue?.codeBlock
            if (codeBlock != null) {
                initializer = codeBlock
            } else {
                initNullValue()
            }
        }
    }

    /**
     * Generate the base accessor functions (such as DSL builder methods) for this parameter.
     * Subclasses override this to provide type-specific accessors.
     */
    fun accessors(): List<FunSpec> {
        return emptyList()
    }

    /**
     * Generate all accessor functions, including:
     * - Base accessors from [accessors]
     * - @DeprecatedDsl annotation on all accessors
     * - @DslAlias alias functions mirroring the primary accessors
     */
    fun allAccessors(): List<FunSpec> {
        val baseAccessors = accessors()
        val deprecatedAnnotation = buildDeprecatedAnnotation()
        val aliases = annotationMetadata.aliases

        // Apply @DeprecatedDsl to all base accessors
        val decoratedAccessors = if (deprecatedAnnotation != null) {
            baseAccessors.map { funSpec ->
                funSpec.toBuilder().addAnnotation(deprecatedAnnotation).build()
            }
        } else {
            baseAccessors
        }

        // Generate alias functions from @DslAlias
        val aliasAccessors = if (aliases.isNotEmpty()) {
            baseAccessors.flatMap { originalFun ->
                aliases.map { alias ->
                    val builder = originalFun.toBuilder(name = alias)
                    deprecatedAnnotation?.let { builder.addAnnotation(it) }
                    builder.build()
                }
            }
        } else {
            emptyList()
        }

        return decoratedAccessors + aliasAccessors
    }

    /**
     * Build a @Deprecated annotation spec if this property has @DeprecatedDsl metadata.
     */
    fun buildDeprecatedAnnotation(): AnnotationSpec? {
        val message = annotationMetadata.deprecatedMessage ?: return null
        val builder = AnnotationSpec.builder(Deprecated::class)
            .addMember("message = %S", message)

        annotationMetadata.deprecatedReplaceWith?.let { replaceWith ->
            builder.addMember(
                "replaceWith = %T(%S)",
                ReplaceWith::class,
                replaceWith
            )
        }

        return builder.build()
    }

    /**
     * Provides validation code to emit in the build() method from @ValidateDsl.
     * Returns null if no validation is needed.
     */
    fun validationStatement(): String? {
        val expression = annotationMetadata.validateExpression ?: return null
        // Replace "it" with the actual property name
        val resolvedExpression = expression.replace("it", propName)
        val message = annotationMetadata.validateMessage
            ?: "Validation failed for property '$propName'"
        return "require($resolvedExpression) { \"$message\" }"
    }

    /**
     * Returns all build-time statements for this property:
     * transformations (distinct, sorted), size validations (@ListDsl/@MapDsl),
     * and custom validations (@ValidateDsl).
     */
    fun buildStatements(): List<String> {
        val statements = mutableListOf<String>()

        // @ListDsl transformations
        if (annotationMetadata.listDslUniqueElements || annotationMetadata.listDslSorted) {
            val transforms = buildList {
                if (annotationMetadata.listDslUniqueElements) add("distinct()")
                if (annotationMetadata.listDslSorted) add("sorted()")
            }.joinToString("?.")
            statements.add("$propName = $propName?.${transforms}")
        }

        // @ListDsl size constraints
        annotationMetadata.listDslMinSize?.let { min ->
            val call = DSL_VALIDATION_CLASS +
                ".requireMinSize(it, $min, \"$propName\")"
            statements.add("$propName?.let { $call }")
        }
        annotationMetadata.listDslMaxSize?.let { max ->
            val call = DSL_VALIDATION_CLASS +
                ".requireMaxSize(it, $max, \"$propName\")"
            statements.add("$propName?.let { $call }")
        }

        // @MapDsl size constraints
        annotationMetadata.mapDslMinSize?.let { min ->
            val call = DSL_VALIDATION_CLASS +
                ".requireMinSize(it, $min, \"$propName\")"
            statements.add("$propName?.let { $call }")
        }
        annotationMetadata.mapDslMaxSize?.let { max ->
            val call = DSL_VALIDATION_CLASS +
                ".requireMaxSize(it, $max, \"$propName\")"
            statements.add("$propName?.let { $call }")
        }

        // @ValidateDsl
        validationStatement()?.let { statements.add(it) }

        return statements
    }

    /**
     * Provide the code snippet used when returning this parameter's value.
     */
    fun propertyValueReturn(): String {
        if (nullableAssignment) return propName

        return when {
            verifyNotNull -> "DslValidation.requireNotNull(::$propName)"
            verifyNotEmpty && isCollection() -> "DslValidation.requireCollectionNotEmpty(::$propName)"
            verifyNotEmpty && isMap() -> "DslValidation.requireMapNotEmpty(::$propName)"
            else -> propName
        }
    }

    enum class IterableType {
        COLLECTION, MAP
    }
}

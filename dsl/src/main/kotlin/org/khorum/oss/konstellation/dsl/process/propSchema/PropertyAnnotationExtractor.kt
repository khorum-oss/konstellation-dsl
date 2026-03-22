package org.khorum.oss.konstellation.dsl.process.propSchema

import com.google.devtools.ksp.symbol.KSAnnotation
import org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata
import org.khorum.oss.konstellation.dsl.utils.AnnotationLookup

/**
 * Extracts [PropertyAnnotationMetadata] from a sequence of KSP annotations.
 * Each annotation type is handled by a dedicated extraction method,
 * following a factory-style pattern for extensibility.
 */
interface PropertyAnnotationExtractor {
    fun extract(annotations: Sequence<KSAnnotation>): PropertyAnnotationMetadata
}

/**
 * Default implementation that delegates each annotation type
 * to a focused extraction function.
 */
class DefaultPropertyAnnotationExtractor : PropertyAnnotationExtractor {

    override fun extract(annotations: Sequence<KSAnnotation>): PropertyAnnotationMetadata {
        val transient = extractTransient(annotations)
        val description = extractDescription(annotations)
        val aliases = extractAliases(annotations)
        val deprecated = extractDeprecated(annotations)
        val validate = extractValidate(annotations)

        return PropertyAnnotationMetadata(
            isTransient = transient.first,
            transientReason = transient.second,
            description = description,
            aliases = aliases,
            deprecatedMessage = deprecated.first,
            deprecatedReplaceWith = deprecated.second,
            validateExpression = validate.first,
            validateMessage = validate.second,
        )
    }

    private fun extractTransient(annotations: Sequence<KSAnnotation>): Pair<Boolean, String?> {
        val ann = AnnotationLookup.findAnnotationByName(annotations, "TransientDsl")
            ?: return false to null
        val reason = AnnotationLookup.findArgumentValue<String>(ann, "reason")
            ?.takeIf { it.isNotBlank() }
        return true to reason
    }

    private fun extractDescription(annotations: Sequence<KSAnnotation>): String? {
        val ann = AnnotationLookup.findAnnotationByName(annotations, "DslDescription")
            ?: return null
        return AnnotationLookup.findArgumentValue<String>(ann, "value")
            ?.takeIf { it.isNotBlank() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractAliases(annotations: Sequence<KSAnnotation>): List<String> {
        val ann = AnnotationLookup.findAnnotationByName(annotations, "DslAlias")
            ?: return emptyList()
        val singleValue = AnnotationLookup.findArgumentValue<String>(ann, "value")
        return if (singleValue != null && singleValue.isNotBlank()) {
            listOf(singleValue)
        } else {
            val listValue = AnnotationLookup.findArgumentValue<List<String>>(ann, "value")
            listValue?.filter { it.isNotBlank() } ?: emptyList()
        }
    }

    private fun extractDeprecated(annotations: Sequence<KSAnnotation>): Pair<String?, String?> {
        val ann = AnnotationLookup.findAnnotationByName(annotations, "DeprecatedDsl")
            ?: return null to null
        val message = AnnotationLookup.findArgumentValue<String>(ann, "message")
            ?.takeIf { it.isNotBlank() }
        val replaceWith = AnnotationLookup.findArgumentValue<String>(ann, "replaceWith")
            ?.takeIf { it.isNotBlank() }
        return message to replaceWith
    }

    private fun extractValidate(annotations: Sequence<KSAnnotation>): Pair<String?, String?> {
        val ann = AnnotationLookup.findAnnotationByName(annotations, "ValidateDsl")
            ?: return null to null
        val expression = AnnotationLookup.findArgumentValue<String>(ann, "expression")
            ?.takeIf { it.isNotBlank() }
        val message = AnnotationLookup.findArgumentValue<String>(ann, "message")
            ?.takeIf { it.isNotBlank() }
        return expression to message
    }
}

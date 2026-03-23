package org.khorum.oss.konstellation.dsl.process.propSchema

import com.google.devtools.ksp.symbol.KSAnnotation
import org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata
import org.khorum.oss.konstellation.dsl.utils.AnnotationLookup

/**
 * Extracts [PropertyAnnotationMetadata] from a sequence of KSP annotations.
 * Each annotation type is handled by a dedicated extraction method,
 * following a factory-style pattern for extensibility.
 */
fun interface PropertyAnnotationExtractor {
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
        val listDsl = extractListDsl(annotations)
        val mapDsl = extractMapDsl(annotations)

        return PropertyAnnotationMetadata(
            isTransient = transient.first,
            transientReason = transient.second,
            description = description,
            aliases = aliases,
            deprecatedMessage = deprecated.first,
            deprecatedReplaceWith = deprecated.second,
            validateExpression = validate.first,
            validateMessage = validate.second,
            listDslMinSize = listDsl?.minSize,
            listDslMaxSize = listDsl?.maxSize,
            listDslUniqueElements = listDsl?.uniqueElements ?: false,
            listDslSorted = listDsl?.sorted ?: false,
            hasListDsl = listDsl != null,
            listDslWithVararg = listDsl?.withVararg,
            listDslWithProvider = listDsl?.withProvider,
            mapDslMinSize = mapDsl?.minSize,
            mapDslMaxSize = mapDsl?.maxSize,
            hasMapDsl = mapDsl != null,
            mapDslWithVararg = mapDsl?.withVararg,
            mapDslWithProvider = mapDsl?.withProvider,
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
        val raw = AnnotationLookup.findAnnotationByName(annotations, "DslAlias")
            ?.let { AnnotationLookup.findArgument(it, "names")?.value }
            ?: return emptyList()
        return when (raw) {
            is String -> if (raw.isNotBlank()) listOf(raw) else emptyList()
            is List<*> -> (raw as List<String>).filter { it.isNotBlank() }
            else -> emptyList()
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

    private fun extractListDsl(annotations: Sequence<KSAnnotation>): ListDslMetadata? {
        val ann = AnnotationLookup.findAnnotationByName(annotations, "ListDsl")
            ?: return null
        val minSize = AnnotationLookup.findArgumentValue<Int>(ann, "minSize") ?: -1
        val maxSize = AnnotationLookup.findArgumentValue<Int>(ann, "maxSize") ?: -1
        val uniqueElements = AnnotationLookup.findArgumentValue<Boolean>(ann, "uniqueElements") ?: false
        val sorted = AnnotationLookup.findArgumentValue<Boolean>(ann, "sorted") ?: false
        val withVararg = AnnotationLookup.findArgumentValue<Boolean>(ann, "withVararg") ?: true
        val withProvider = AnnotationLookup.findArgumentValue<Boolean>(ann, "withProvider") ?: true
        return ListDslMetadata(
            minSize = minSize.takeIf { it >= 0 },
            maxSize = maxSize.takeIf { it >= 0 },
            uniqueElements = uniqueElements,
            sorted = sorted,
            withVararg = withVararg,
            withProvider = withProvider
        )
    }

    private fun extractMapDsl(annotations: Sequence<KSAnnotation>): MapDslMetadata? {
        val ann = AnnotationLookup.findAnnotationByName(annotations, "MapDsl")
            ?: return null
        val minSize = AnnotationLookup.findArgumentValue<Int>(ann, "minSize") ?: -1
        val maxSize = AnnotationLookup.findArgumentValue<Int>(ann, "maxSize") ?: -1
        val withVararg = AnnotationLookup.findArgumentValue<Boolean>(ann, "withVararg") ?: true
        val withProvider = AnnotationLookup.findArgumentValue<Boolean>(ann, "withProvider") ?: true
        return MapDslMetadata(
            minSize = minSize.takeIf { it >= 0 },
            maxSize = maxSize.takeIf { it >= 0 },
            withVararg = withVararg,
            withProvider = withProvider
        )
    }

    private data class ListDslMetadata(
        val minSize: Int?,
        val maxSize: Int?,
        val uniqueElements: Boolean,
        val sorted: Boolean,
        val withVararg: Boolean,
        val withProvider: Boolean
    )

    private data class MapDslMetadata(
        val minSize: Int?,
        val maxSize: Int?,
        val withVararg: Boolean,
        val withProvider: Boolean
    )
}

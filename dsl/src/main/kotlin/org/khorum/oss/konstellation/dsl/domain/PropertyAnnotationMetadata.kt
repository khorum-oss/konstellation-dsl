package org.khorum.oss.konstellation.dsl.domain

/**
 * Holds metadata extracted from new-style annotations on a property.
 * Used by the property schema pipeline to generate appropriate accessors and validation.
 */
data class PropertyAnnotationMetadata(
    val isTransient: Boolean = false,
    val transientReason: String? = null,
    val description: String? = null,
    val aliases: List<String> = emptyList(),
    val deprecatedMessage: String? = null,
    val deprecatedReplaceWith: String? = null,
    val validateExpression: String? = null,
    val validateMessage: String? = null,
)

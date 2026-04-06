package org.khorum.oss.konstellation.dsl.domain

/**
 * Holds metadata extracted from new-style annotations on a property.
 * Used by the property schema pipeline to generate appropriate accessors and validation.
 */
data class PropertyAnnotationMetadata(
    val isTransient: Boolean = false,
    val transientReason: String? = null,
    val description: String? = null,
    val docString: String? = null,
    val aliases: List<String> = emptyList(),
    val deprecatedMessage: String? = null,
    val deprecatedReplaceWith: String? = null,
    val validateExpression: String? = null,
    val validateMessage: String? = null,
    // @ListDsl metadata
    val listDslMinSize: Int? = null,
    val listDslMaxSize: Int? = null,
    val listDslUniqueElements: Boolean = false,
    val listDslSorted: Boolean = false,
    val hasListDsl: Boolean = false,
    val listDslWithVararg: Boolean? = null,
    val listDslWithProvider: Boolean? = null,
    // @MapDsl metadata
    val mapDslMinSize: Int? = null,
    val mapDslMaxSize: Int? = null,
    val hasMapDsl: Boolean = false,
    val mapDslWithVararg: Boolean? = null,
    val mapDslWithProvider: Boolean? = null,
) {
    /**
     * Returns the effective documentation string for this property.
     * `@DslDescription` takes precedence; falls back to the source KDoc comment.
     */
    val effectiveDescription: String? get() = description ?: docString
}

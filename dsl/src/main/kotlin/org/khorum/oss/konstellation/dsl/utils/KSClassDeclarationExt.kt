package org.khorum.oss.konstellation.dsl.utils

import com.google.devtools.ksp.symbol.KSClassDeclaration
import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.MapGroupType

/**
 * Extension functions for [KSClassDeclaration] to check if it is a DSL class
 * and to retrieve group type information from its annotations.
 */
fun KSClassDeclaration?.isGroupDsl(): Boolean {
    if (this == null) return false
    return AnnotationLookup.anyAnnotationArgMatches(
        annotations, GeneratedDsl::class, GeneratedDsl::withListGroup.name
    ) { it == true }
}

/**
 * Extension function to retrieve the group type from the annotations of a [KSClassDeclaration].
 */
@Suppress("ReturnCount")
fun KSClassDeclaration?.mapGroupType(): MapGroupType? {
    if (this == null) return null
    val annotation = AnnotationLookup.findAnnotation(annotations, GeneratedDsl::class) ?: return null
    val value = AnnotationLookup.findArgument(annotation, GeneratedDsl::withMapGroup.name)?.value ?: return null
    return MapGroupType.valueOf(value.toString().uppercase())
}

package org.khorum.oss.konstellation.dsl.utils

import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Extension functions for [KSClassDeclaration] to check if it is a DSL class
 * and to retrieve group type information from its annotations.
 */
fun KSClassDeclaration?.isGroupDsl(): Boolean {
    if (this == null) return false
    return AnnotationLookup.hasAnnotationByName(annotations, "GeneratedDsl")
}

/**
 * Extension function to check whether a [KSClassDeclaration] has a `@GeneratedDsl` annotation
 * and can participate in map group generation.
 */
fun KSClassDeclaration?.hasMapDsl(): Boolean {
    if (this == null) return false
    return AnnotationLookup.hasAnnotationByName(annotations, "GeneratedDsl")
}

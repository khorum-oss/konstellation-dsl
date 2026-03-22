package org.khorum.oss.konstellation.dsl.utils

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSValueArgument
import kotlin.reflect.KClass

/**
 * Centralized utility for looking up KSP annotations and their arguments.
 * Eliminates duplicated annotation-querying patterns across the codebase.
 */
object AnnotationLookup {

    /**
     * Find a single annotation matching the given class on the provided annotation sequence.
     */
    fun findAnnotation(annotations: Sequence<KSAnnotation>, annotationClass: KClass<*>): KSAnnotation? {
        val simpleName = annotationClass.simpleName ?: return null
        return annotations.firstOrNull { it.shortName.asString() == simpleName }
    }

    /**
     * Find a single annotation matching the given simple name on the provided annotation sequence.
     * Useful for annotations that may not be on the processor's compile classpath.
     */
    fun findAnnotationByName(annotations: Sequence<KSAnnotation>, simpleName: String): KSAnnotation? {
        return annotations.firstOrNull { it.shortName.asString() == simpleName }
    }

    /**
     * Check whether any annotation matching the given simple name exists.
     */
    fun hasAnnotationByName(annotations: Sequence<KSAnnotation>, simpleName: String): Boolean {
        return annotations.any { it.shortName.asString() == simpleName }
    }

    /**
     * Filter all annotations matching the given class from the provided annotation sequence.
     */
    fun filterAnnotations(annotations: Sequence<KSAnnotation>, annotationClass: KClass<*>): Sequence<KSAnnotation> {
        val simpleName = annotationClass.simpleName ?: return emptySequence()
        return annotations.filter { it.shortName.asString() == simpleName }
    }

    /**
     * Check whether any annotation matching the given class exists.
     */
    fun hasAnnotation(annotations: Sequence<KSAnnotation>, annotationClass: KClass<*>): Boolean {
        val simpleName = annotationClass.simpleName ?: return false
        return annotations.any { it.shortName.asString() == simpleName }
    }

    /**
     * Find a specific argument by name within an annotation.
     */
    fun findArgument(annotation: KSAnnotation?, argumentName: String): KSValueArgument? {
        return annotation?.arguments?.firstOrNull { it.name?.asString() == argumentName }
    }

    /**
     * Get the value of a specific argument by name within an annotation, cast to [T].
     * Returns null if the annotation, argument, or cast fails.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> findArgumentValue(annotation: KSAnnotation?, argumentName: String): T? {
        return findArgument(annotation, argumentName)?.value as? T
    }

    /**
     * Check whether any annotation of the given class has an argument with the specified name
     * whose value matches the provided predicate.
     */
    fun anyAnnotationArgMatches(
        annotations: Sequence<KSAnnotation>,
        annotationClass: KClass<*>,
        argumentName: String,
        predicate: (Any?) -> Boolean
    ): Boolean {
        return filterAnnotations(annotations, annotationClass)
            .any { annotation ->
                val arg = findArgument(annotation, argumentName)
                arg != null && predicate(arg.value)
            }
    }
}

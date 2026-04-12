package org.khorum.oss.konstellation.dsl.process

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Holder for the current KSP [Resolver] during a single `DslProcessor.process` pass.
 *
 * `@GeneratedDsl` has `SOURCE` retention, so when a parent DSL in module A references
 * a nested DSL type compiled into module B's JAR, KSP in module A cannot see the
 * `@GeneratedDsl` annotation on the imported type (source-retention annotations are
 * stripped from compiled class files). Without a fallback, the parent's property
 * falls through to `DefaultPropSchema` and no builder accessor is generated for the
 * nested DSL.
 *
 * This context exposes the active [Resolver] so property schema adapters can fall
 * back to looking up `${type.qualifiedName}DslBuilder` via
 * [Resolver.getClassDeclarationByName]. If a generated builder class exists on the
 * classpath for the property type, we treat the type as a DSL builder type even
 * when its `@GeneratedDsl` annotation is no longer visible.
 *
 * The value is intentionally stored as a plain nullable property. KSP runs each
 * processor pass single-threaded, and [withResolver] is responsible for resetting
 * the value after the pass completes so no state leaks between invocations.
 */
object ResolverContext {
    /** The active resolver for the in-progress DSL generation, or null outside a pass. */
    var current: Resolver? = null
        private set

    /**
     * Run [block] with [resolver] installed as [current]. Restores the previous value
     * (typically null) when [block] returns or throws.
     */
    fun <T> withResolver(resolver: Resolver, block: () -> T): T {
        val previous = current
        current = resolver
        return try {
            block()
        } finally {
            current = previous
        }
    }

    /**
     * Returns true if the current resolver can find a class named
     * `${declaration.qualifiedName}DslBuilder` on the classpath. Used as a
     * fallback for detecting nested DSL builder types whose `@GeneratedDsl`
     * source-retention annotation is no longer visible (cross-module scenarios).
     *
     * Returns false when no resolver is installed or the declaration has no
     * qualified name.
     */
    fun hasGeneratedDslBuilderFor(declaration: KSClassDeclaration): Boolean {
        val resolver = current ?: return false
        val qualifiedName = declaration.qualifiedName?.asString() ?: return false
        val builderName = "${qualifiedName}DslBuilder"
        val ksName = resolver.getKSNameFromString(builderName)
        return resolver.getClassDeclarationByName(ksName) != null
    }
}

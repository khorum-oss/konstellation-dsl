package org.khorum.oss.konstellation.crossModuleTest

import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.RootDsl

/**
 * Nested DSL class defined in a separate Gradle module. When a parent DSL in
 * another module references this type via a property, the parent's generated
 * builder should expose a builder accessor named after the property
 * (e.g. `nested`), not the class (`crossModuleNested`).
 *
 * This class exists to verify cross-module DSL detection continues to work
 * after @GeneratedDsl and @RootDsl annotation retention changes.
 */
@RootDsl
@GeneratedDsl
data class CrossModuleNested(
    val label: String? = null,
    val count: Int? = null,
)

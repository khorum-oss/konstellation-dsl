package org.khorum.oss.konstellation.dsl.builder

import org.khorum.oss.konstellation.dsl.common.ExcludeFromCoverage

/**
 * A marker annotation for DSLs in the Picard library.
 * This annotation is used to restrict the scope of DSL builders
 * and prevent accidental misuse of DSL elements outside their intended context.
 */
@ExcludeFromCoverage
@DslMarker
annotation class PicardDSLMarker

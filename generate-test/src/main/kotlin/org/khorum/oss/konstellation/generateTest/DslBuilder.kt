package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.konstellation.metaDsl.CoreDslBuilder

@TestDslMarker
interface DslBuilder<T> : CoreDslBuilder<T> {
    override fun build(): T
}

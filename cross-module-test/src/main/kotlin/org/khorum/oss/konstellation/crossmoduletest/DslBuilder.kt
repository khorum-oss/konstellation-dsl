package org.khorum.oss.konstellation.crossmoduletest

fun interface DslBuilder<T> {
    fun build(): T
}

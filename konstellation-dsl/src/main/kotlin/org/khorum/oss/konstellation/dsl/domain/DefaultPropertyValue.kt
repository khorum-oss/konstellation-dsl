package org.khorum.oss.konstellation.dsl.domain

import com.squareup.kotlinpoet.CodeBlock

data class DefaultPropertyValue(
    val rawValue: String,
    val codeBlock: CodeBlock,
    val packageName: String,
    val className: String,
    val booleanAccessorConfig: BooleanAccessorConfig? = null
) {
    fun importString(): String? =
        if (packageName.isNotEmpty() && className.isNotEmpty()) "$packageName.$className" else null
}

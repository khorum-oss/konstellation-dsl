package org.khorum.oss.konstellation.dsl.domain

import com.squareup.kotlinpoet.TypeName

/**
 * Represents a method annotated with `@InjectDslMethod` that should be
 * copied into the generated DSL builder.
 */
data class InjectedMethod(
    val name: String,
    val parameters: List<InjectedMethodParameter>,
    val returnType: TypeName,
    val body: String
)

data class InjectedMethodParameter(
    val name: String,
    val type: TypeName
)

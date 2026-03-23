package org.khorum.oss.konstellation.dsl.process.root

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import org.khorum.oss.konstellation.dsl.builder.kotlinPoet
import org.khorum.oss.konstellation.dsl.domain.BuilderConfig

/**
 * Interface for generating the root DSL function.
 */
interface RootFunctionGenerator {
    fun generate(
        domain: KSClassDeclaration,
        builderConfig: BuilderConfig,
        customName: String? = null
    ): FunSpec
}

/**
 * Default implementation of [RootFunctionGenerator].
 * This class generates a root function for the DSL that initializes a builder for the given domain.
 */
class DefaultRootFunctionGenerator : RootFunctionGenerator {
    override fun generate(
        domain: KSClassDeclaration,
        builderConfig: BuilderConfig,
        customName: String?
    ): FunSpec = kotlinPoet {
        function {
            val domainClassName = domain.toClassName()
            val domainBuilderClassName =
                ClassName(domainClassName.packageName, "${domainClassName.simpleName}DslBuilder")
            funName = customName ?: domain.simpleName.asString().replaceFirstChar { it.lowercase() }

            param {
                lambdaType {
                    receiver = domainBuilderClassName
                }
            }

            returns = domainClassName

            statements {
                addLine("val builder = %T()", domainBuilderClassName)
                addLine("builder.block()")
                addLine("return builder.build()")
            }
        }
    }
}

package org.khorum.oss.konstellation.dsl.process.root

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.writeTo
import org.khorum.oss.konstellation.dsl.builder.kotlinPoet
import org.khorum.oss.konstellation.dsl.domain.BuilderConfig
import org.khorum.oss.konstellation.dsl.process.DslFileWriter
import org.khorum.oss.konstellation.dsl.utils.VLoggable

/**
 * Interface for generating the root DSL accessor.
 */
interface RootDslAccessorGenerator : DslFileWriter, VLoggable {
    override fun logId(): String? = RootDslAccessorGenerator::class.simpleName

    fun generate(
        codeGenerator: CodeGenerator,
        domains: List<Triple<KSClassDeclaration, String?, String?>>,
        builderConfig: BuilderConfig
    )
}

/**
 * Default implementation of [RootDslAccessorGenerator].
 */
class DefaultRootDslAccessorGenerator(
    private val rootFunctionGenerator: DefaultRootFunctionGenerator = DefaultRootFunctionGenerator(),
) : RootDslAccessorGenerator {
    override fun generate(
        codeGenerator: CodeGenerator,
        domains: List<Triple<KSClassDeclaration, String?, String?>>,
        builderConfig: BuilderConfig
    ) {
        val functions = domains
            .flatMap { (domain, name, alias) ->
                buildList {
                    add(rootFunctionGenerator.generate(domain, builderConfig, name))
                    if (alias != null) {
                        add(rootFunctionGenerator.generate(domain, builderConfig, alias))
                    }
                }
            }

        val fileSpec = kotlinPoet {
            file {
                className = ClassName(builderConfig.rootDslFileClasspath(), "RootDslAccessor")
                functions(functions)
            }
        }

        val containingFiles = domains.mapNotNull { it.first.containingFile }.toTypedArray()
        val dependencies = Dependencies(aggregating = false, sources = containingFiles)

        fileSpec.writeTo(codeGenerator, dependencies)
        logger.debug("file written: RootDslAccessor", tier = 1)
    }
}

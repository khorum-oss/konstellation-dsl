package org.khorum.oss.konstellation.dsl

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import org.khorum.oss.konstellation.dsl.common.ExcludeFromCoverage

/**
 * Provider for the DSL symbol processor.
 */
@ExcludeFromCoverage
@AutoService(SymbolProcessorProvider::class)
class DslProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) =
        DslProcessor(environment.codeGenerator, environment.options)
}

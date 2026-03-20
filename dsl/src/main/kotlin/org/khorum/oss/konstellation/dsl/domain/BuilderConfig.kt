package org.khorum.oss.konstellation.dsl.domain

import org.khorum.oss.konstellation.dsl.exception.RequiredDataException
import org.khorum.oss.konstellation.dsl.utils.Logger

/**
 * Configuration for the DSL builder.
 *
 * @property map A map containing configuration properties.
 * @property logger A logger instance for logging debug information.
 *
 * @constructor Creates a [BuilderConfig] instance with the provided map and logger.
 *
 * @throws RuntimeException if required properties are missing from the map.
 */
class BuilderConfig(
    map: Map<String, String?>,
    private val logger: Logger
) {
    /**
     * Disables generation.
     */
    val isIgnored: Boolean = map["isIgnored"]?.toBoolean() ?: false

    /**
     * The project root class path [e.g. org.khorum.oss.target.folder]
     */
    val projectRootClasspath: String = map["projectRootClasspath"]
        ?: throw RequiredDataException("projectRootClasspath")

    /**
     * The path to the project defined DslBuilder that extends [org.khorum.oss.konstellation.dsl.CoreDslBuilder]
     */
    val dslBuilderClasspath: String = map["dslBuilderClasspath"]
        ?: throw RequiredDataException("dslBuilderClasspath")

    /**
     * The class path for where the generated root DSL file will go.
     * Defaults to [projectRootClasspath]
     */
    private val rootDslFileClasspath: String? = map["rootDslFileClasspath"]

    /**
     * The class (with class path) of the marker class (e.g. org.khorum.oss.exampleProj.ExampleProjMarkerClass
     */
    val dslMarkerClass: String? = map["dslMarkerClass"]

    /**
     * Returns the root DSL file classpath if provided or the default of the project root.
     */
    fun rootDslFileClasspath(): String = rootDslFileClasspath ?: projectRootClasspath

    fun printDebug() {
        logger.debug("rootDslFileClasspath: ${rootDslFileClasspath()}")
        logger.debug("dslBuilderClasspath: $dslBuilderClasspath")
        logger.debug("dslMarkerClass: $dslMarkerClass")
    }
}

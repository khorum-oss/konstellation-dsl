package org.khorum.oss.konstellation.dsl.process.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import org.khorum.oss.konstellation.dsl.builder.AnnotationDecorator
import org.khorum.oss.konstellation.dsl.builder.KPTypeSpecBuilder
import org.khorum.oss.konstellation.dsl.domain.DomainConfig
import org.khorum.oss.konstellation.dsl.utils.AnnotationLookup
import org.khorum.oss.konstellation.dsl.utils.VLoggable

/**
 * Abstract class for generating DSL groups.
 * @param config The configuration for the group generator.
 * @param annotationDecorator The decorator for handling annotations in the DSL group.
 */
abstract class GroupGenerator(
    val config: Config,
    val annotationDecorator: AnnotationDecorator
) : VLoggable {

    /**
     * Configuration class for the group generator.
     * This class holds the necessary information to generate the DSL group.
     * @param namespace The namespace for the DSL group.
     * @property annotationName The annotation simple name to check on the domain class.
     * @property templates The templates used for generating the DSL group.
     * @property propertyTypeAssigner A function to assign the type variable for the DSL group property.
     * @property builtTypeAssigner A function to assign the type variable for the built DSL group.
     */
    class Config(
        val namespace: Namespace,
        val annotationName: String,
        val templates: Templates,
        val propertyTypeAssigner: (typeVariable: TypeName?, domainClassName: ClassName) -> TypeName,
        val builtTypeAssigner: (typeVariable: TypeName?, domainClassName: ClassName) -> TypeName,
    )

    /**
     * Namespace class for the group generator.
     * This class holds the namespace information for the DSL group.
     *
     * @property checkName The name used to check if the group is applicable.
     * @property typeName The name of the type for the DSL group.
     * @property typeVariable An optional type variable for the DSL group.
     */
    class Namespace(
        val checkName: String,
        val typeName: String,
        val typeVariable: String? = null
    )

    /**
     * Templates class for the group generator.
     * This class holds the templates used for generating the DSL group.
     *
     * @property prop The property template for the DSL group.
     * @property itemsReturn The return statement for the items function in the DSL group.
     * @property builderAdd The statement for adding an item to the DSL group builder.
     */
    class Templates(
        val prop: String,
        val itemsReturn: String,
        val builderAdd: String
    )

    /**
     * Checks if the given domain configuration is a group based on the presence of the
     * configured annotation (e.g. `@ListDsl`, `@MapDsl`) on the domain class.
     *
     * @param domainConfig The configuration of the domain to check.
     * @return True if the domain is a group, false otherwise.
     */
    protected fun isGroup(domainConfig: DomainConfig): Boolean {
        val isGroup = AnnotationLookup.hasAnnotationByName(
            domainConfig.domain.annotations, config.annotationName
        )
        logger.debug("[DECISION] ${config.namespace.checkName}: $isGroup", tier = 1)

        val typeName = config.namespace.typeName

        logger.debug("$typeName domain", tier = 1, branch = true)
        return isGroup
    }

    /**
     * Generates the DSL group using the provided builder and domain configuration.
     * This method will create a nested type with properties and functions based on the group configuration.
     *
     * @param builder The KotlinPoet builder to generate the DSL group.
     * @param domainConfig The configuration of the domain for which the DSL group is generated.
     */
    fun generate(builder: KPTypeSpecBuilder, domainConfig: DomainConfig, effectiveClassDoc: String? = null) = with(builder) {
        val isGroup = isGroup(domainConfig)

        if (!isGroup) return@with

        val domainClassName = domainConfig.domainClassName

        nested {
            val typeVariable = config.namespace.typeVariable?.let { TypeVariableName(it) }
            addType {
                name = config.namespace.typeName
                typeVariable?.let {
                    typeVariables(it)
                }
                effectiveClassDoc?.let { kdoc(it) }
                annotations {
                    annotationDecorator
                        .createDslMarkerIfAvailable(domainConfig.builderConfig.dslMarkerClass)
                        ?.also { annotation(it) }
                }
                properties {
                    add {
                        protected()
                        name = "items"
                        type(config.propertyTypeAssigner(typeVariable, domainClassName))
                        initializer(config.templates.prop)
                    }
                }
                functions {
                    add {
                        funName = "items"
                        returns = config.builtTypeAssigner(typeVariable, domainClassName)
                        statements {
                            addLine(config.templates.itemsReturn)
                        }
                    }

                    add {
                        val simpleName = domainClassName.simpleName
                        funName = simpleName.first().lowercase() + simpleName.substring(1)

                        typeVariable?.let {
                            param {
                                name = "key"
                                type(it, nullable = false)
                            }
                        }

                        param {
                            lambdaType {
                                receiver = domainConfig.builderClassName
                            }
                        }
                        statements {
                            addLine(config.templates.builderAdd, domainConfig.builderClassName)
                        }
                    }
                }
            }
        }
    }
}


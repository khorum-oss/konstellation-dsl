package org.khorum.oss.konstellation.dsl.props

import com.squareup.kotlinpoet.CodeBlock
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.schema.BooleanPropSchema
import org.junit.jupiter.api.Test
import org.khorum.oss.konstellation.dsl.domain.BooleanAccessorConfig
import org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue

class BooleanParamTest : UnitSim() {

    @Test
    fun `toPropertySpec - happy path - #scenario`() = test {
        given {
            val param = BooleanPropSchema("test")

            expect { "protected var test: kotlin.Boolean? = null" }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = BooleanPropSchema("test", true)

            expect {
                """
                    |public fun test(on: kotlin.Boolean = true) {
                    |  this.test = on
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }

    @Test
    fun `toPropertySpec - non-nullable boolean`() = test {
        given {
            val param = BooleanPropSchema("active", nullableAssignment = false)
            expect { true }
            whenever { param.toPropertySpec().toString().contains("Boolean") }
        }
    }

    @Test
    fun `accessors - with defaultValue false`() = test {
        given {
            val param = BooleanPropSchema("enabled", defaultValue = DefaultPropertyValue(
                rawValue = "false",
                codeBlock = CodeBlock.of("%L", false),
                packageName = "kotlin",
                className = "Boolean"
            )
            )
            expect { true }
            whenever { param.accessors().first().toString().contains("false") }
        }
    }

    @Test
    fun `propertyValueReturn - nullable returns propName`() = test {
        given {
            val param = BooleanPropSchema("active", nullableAssignment = true)
            expect { "active" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `propertyValueReturn - non-nullable returns requireNotNull`() = test {
        given {
            val param = BooleanPropSchema("active", nullableAssignment = false)
            expect { "DslValidation.requireNotNull(::active)" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `isCollection returns false`() = test {
        given {
            val param = BooleanPropSchema("active")
            expect { false }
            whenever { param.isCollection() }
        }
    }

    @Test
    fun `isMap returns false`() = test {
        given {
            val param = BooleanPropSchema("active")
            expect { false }
            whenever { param.isMap() }
        }
    }

    @Test
    fun `accessors - with BooleanAccessorConfig generates valid and negation functions`() = test {
        given {
            val config = BooleanAccessorConfig(
                validTemplate = "SELF",
                negationTemplate = "NOT"
            )
            val param = BooleanPropSchema("isCool", defaultValue = DefaultPropertyValue(
                rawValue = "true",
                codeBlock = CodeBlock.of("%L", true),
                packageName = "",
                className = "",
                booleanAccessorConfig = config
            ))

            expect { 2 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `accessors - valid function name from SELF template`() = test {
        given {
            val config = BooleanAccessorConfig(
                validTemplate = "SELF",
                negationTemplate = "NOT"
            )
            val param = BooleanPropSchema("isCool", defaultValue = DefaultPropertyValue(
                rawValue = "true",
                codeBlock = CodeBlock.of("%L", true),
                packageName = "",
                className = "",
                booleanAccessorConfig = config
            ))

            expect { "isCool" }
            whenever { param.accessors().first().name }
        }
    }

    @Test
    fun `accessors - negation function name from NOT template`() = test {
        given {
            val config = BooleanAccessorConfig(
                validTemplate = "SELF",
                negationTemplate = "NOT"
            )
            val param = BooleanPropSchema("isCool", defaultValue = DefaultPropertyValue(
                rawValue = "true",
                codeBlock = CodeBlock.of("%L", true),
                packageName = "",
                className = "",
                booleanAccessorConfig = config
            ))

            expect { "notIsCool" }
            whenever { param.accessors().last().name }
        }
    }

    @Test
    fun `accessors - negation function body contains negation`() = test {
        given {
            val config = BooleanAccessorConfig(
                validTemplate = "SELF",
                negationTemplate = "NOT"
            )
            val param = BooleanPropSchema("isCool", defaultValue = DefaultPropertyValue(
                rawValue = "true",
                codeBlock = CodeBlock.of("%L", true),
                packageName = "",
                className = "",
                booleanAccessorConfig = config
            ))

            expect { true }
            whenever { param.accessors().last().toString().contains("!") }
        }
    }

    @Test
    fun `accessors - NONE negation template generates only valid function`() = test {
        given {
            val config = BooleanAccessorConfig(
                validTemplate = "SELF",
                negationTemplate = "NONE"
            )
            val param = BooleanPropSchema("enabled", defaultValue = DefaultPropertyValue(
                rawValue = "false",
                codeBlock = CodeBlock.of("%L", false),
                packageName = "",
                className = "",
                booleanAccessorConfig = config
            ))

            expect { 1 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `accessors - SELF negation with WITH valid generates paired functions`() = test {
        given {
            val config = BooleanAccessorConfig(
                validTemplate = "WITH",
                negationTemplate = "SELF"
            )
            val param = BooleanPropSchema("withoutMonthly", defaultValue = DefaultPropertyValue(
                rawValue = "false",
                codeBlock = CodeBlock.of("%L", false),
                packageName = "",
                className = "",
                booleanAccessorConfig = config
            ))

            expect { "withMonthly" }
            whenever { param.accessors().first().name }
        }
    }

    @Test
    fun `accessors - no defaultValue uses true as default parameter`() = test {
        given {
            val param = BooleanPropSchema("active", defaultValue = null)

            expect { true }
            whenever { param.accessors().first().toString().contains("true") }
        }
    }

    @Test
    fun `accessors - NONE valid template generates only negation function`() = test {
        given {
            val config = BooleanAccessorConfig(
                validTemplate = "NONE",
                negationTemplate = "NOT"
            )
            val param = BooleanPropSchema("enabled", defaultValue = DefaultPropertyValue(
                rawValue = "true",
                codeBlock = CodeBlock.of("%L", true),
                packageName = "",
                className = "",
                booleanAccessorConfig = config
            ))

            expect { 1 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `accessors - NONE valid only generates negation with correct name`() = test {
        given {
            val config = BooleanAccessorConfig(
                validTemplate = "NONE",
                negationTemplate = "NOT"
            )
            val param = BooleanPropSchema("enabled", defaultValue = DefaultPropertyValue(
                rawValue = "true",
                codeBlock = CodeBlock.of("%L", true),
                packageName = "",
                className = "",
                booleanAccessorConfig = config
            ))

            expect { "notEnabled" }
            whenever { param.accessors().first().name }
        }
    }

    @Test
    fun `accessors - both NONE generates no functions`() = test {
        given {
            val config = BooleanAccessorConfig(
                validTemplate = "NONE",
                negationTemplate = "NONE"
            )
            val param = BooleanPropSchema("enabled", defaultValue = DefaultPropertyValue(
                rawValue = "true",
                codeBlock = CodeBlock.of("%L", true),
                packageName = "",
                className = "",
                booleanAccessorConfig = config
            ))

            expect { 0 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `accessors - backward compat when no config`() = test {
        given {
            val param = BooleanPropSchema("enabled", defaultValue = DefaultPropertyValue(
                rawValue = "false",
                codeBlock = CodeBlock.of("%L", false),
                packageName = "",
                className = ""
            ))

            expect { 1 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `toPropertySpec with defaultValue codeBlock`() = test {
        given {
            val defaultValue = DefaultPropertyValue(
                rawValue = "true",
                codeBlock = CodeBlock.of("%L", true),
                packageName = "kotlin",
                className = "Boolean"
            )
            val param = BooleanPropSchema("enabled", defaultValue = defaultValue)
            expect { true }
            whenever { param.toPropertySpec().toString().contains("true") }
        }
    }
}

package org.khorum.oss.konstellation.dsl.props

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue
import org.khorum.oss.konstellation.dsl.schema.DefaultPropSchema
import org.junit.jupiter.api.Test

class DefaultParamTest : UnitSim() {
    val propTypeName = String::class.asTypeName()
    private val testResponseClassName = String::class.asClassName()

    @Test
    fun `toPropertySpec - happy path - #scenario`() = test {
        given {
            val param = DefaultPropSchema("test", propTypeName)

            expect { "public var test: $testResponseClassName? = null" }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `toPropertySpec - non-nullable assignment still produces nullable property spec`() = test {
        given {
            val param = DefaultPropSchema("test", propTypeName, nullableAssignment = false)

            expect { "public var test: $testResponseClassName? = null" }

            whenever { param.toPropertySpec().toString().trimIndent() }
        }
    }

    @Test
    fun `toPropertySpec - with defaultValue uses codeBlock initializer`() = test {
        given {
            val defaultValue = DefaultPropertyValue(
                rawValue = "\"hello\"",
                codeBlock = CodeBlock.of("%S", "hello"),
                packageName = "kotlin",
                className = "String"
            )
            val param = DefaultPropSchema("test", propTypeName, defaultValue = defaultValue)

            expect { "public var test: $testResponseClassName? = \"hello\"" }

            whenever { param.toPropertySpec().toString().trimIndent() }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = DefaultPropSchema("test", propTypeName, true)

            expect { true }

            whenever { param.accessors().isEmpty() }
        }
    }

    @Test
    fun `propertyValueReturn - nullable assignment returns propName`() = test {
        given {
            val param = DefaultPropSchema("test", propTypeName, nullableAssignment = true)

            expect { "test" }

            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `propertyValueReturn - non-nullable with verifyNotNull returns requireNotNull`() = test {
        given {
            val param = DefaultPropSchema("test", propTypeName, nullableAssignment = false)

            expect { "DslValidation.requireNotNull(::test)" }

            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `isCollection returns false for default schema`() = test {
        given {
            val param = DefaultPropSchema("test", propTypeName)
            expect { false }
            whenever { param.isCollection() }
        }
    }

    @Test
    fun `isMap returns false for default schema`() = test {
        given {
            val param = DefaultPropSchema("test", propTypeName)
            expect { false }
            whenever { param.isMap() }
        }
    }
}

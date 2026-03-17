package org.khorum.oss.konstellation.dsl.domain

import com.squareup.kotlinpoet.CodeBlock
import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DefaultPropertyValueTest : UnitSim() {

    private fun createValue(
        rawValue: String = "defaultVal",
        codeBlock: CodeBlock = CodeBlock.of("\"defaultVal\""),
        packageName: String = "org.khorum.oss.test",
        className: String = "TestClass"
    ) = DefaultPropertyValue(rawValue, codeBlock, packageName, className)

    @Nested
    inner class ImportString {
        @Test
        fun `concatenates packageName and className correctly`() = test {
            given {
                val value = createValue()

                expect { "org.khorum.oss.test.TestClass" }

                whenever { value.importString() }
            }
        }

        @Test
        fun `handles nested package names`() = test {
            given {
                val value = createValue(
                    packageName = "org.khorum.oss.deep.nested",
                    className = "DeepClass"
                )

                expect { "org.khorum.oss.deep.nested.DeepClass" }

                whenever { value.importString() }
            }
        }
    }

    @Nested
    inner class DataClassBehavior {
        @Test
        fun `equality based on properties`() = test {
            given {
                val codeBlock = CodeBlock.of("\"defaultVal\"")
                val value1 = DefaultPropertyValue("defaultVal", codeBlock, "org.test", "MyClass")
                val value2 = DefaultPropertyValue("defaultVal", codeBlock, "org.test", "MyClass")

                expect { true }

                whenever { value1 == value2 }
            }
        }

        @Test
        fun `copy preserves values`() = test {
            given {
                val original = createValue()
                val copied = original.copy(className = "CopiedClass")

                expect { "org.khorum.oss.test.CopiedClass" }

                whenever { copied.importString() }
            }
        }
    }
}

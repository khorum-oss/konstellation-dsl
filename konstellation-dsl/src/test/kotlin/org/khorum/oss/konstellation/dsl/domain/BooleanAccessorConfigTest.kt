package org.khorum.oss.konstellation.dsl.domain

import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BooleanAccessorConfigTest : UnitSim() {

    @Nested
    inner class ResolveValidFunctionName {
        @Test
        fun `SELF template returns property name`() = test {
            given {
                val config = BooleanAccessorConfig(validTemplate = "SELF")

                expect { "isCool" }
                whenever { config.resolveValidFunctionName("isCool") }
            }
        }

        @Test
        fun `NONE template returns null`() = test {
            given {
                val config = BooleanAccessorConfig(validTemplate = "NONE")

                expect { null }
                whenever { config.resolveValidFunctionName("isCool") }
            }
        }

        @Test
        fun `explicit function name overrides template`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validFunctionName = "myCustomValid",
                    validTemplate = "IS"
                )

                expect { "myCustomValid" }
                whenever { config.resolveValidFunctionName("cool") }
            }
        }

        @Test
        fun `IS template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(validTemplate = "IS")

                expect { "isCool" }
                whenever { config.resolveValidFunctionName("cool") }
            }
        }

        @Test
        fun `WITH template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(validTemplate = "WITH")

                expect { "withMonthly" }
                whenever { config.resolveValidFunctionName("monthly") }
            }
        }

        @Test
        fun `null template returns property name as default`() = test {
            given {
                val config = BooleanAccessorConfig(validTemplate = null)

                expect { "enabled" }
                whenever { config.resolveValidFunctionName("enabled") }
            }
        }
    }

    @Nested
    inner class ResolveNegationFunctionName {
        @Test
        fun `SELF template returns property name`() = test {
            given {
                val config = BooleanAccessorConfig(negationTemplate = "SELF")

                expect { "isCool" }
                whenever { config.resolveNegationFunctionName("isCool") }
            }
        }

        @Test
        fun `NONE template returns null`() = test {
            given {
                val config = BooleanAccessorConfig(negationTemplate = "NONE")

                expect { null }
                whenever { config.resolveNegationFunctionName("isCool") }
            }
        }

        @Test
        fun `NOT template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "SELF",
                    negationTemplate = "NOT"
                )

                expect { "notIsCool" }
                whenever { config.resolveNegationFunctionName("isCool") }
            }
        }

        @Test
        fun `IS_NOT template with IS valid extracts semantic name`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "IS",
                    negationTemplate = "IS_NOT"
                )

                expect { "isNotCool" }
                whenever { config.resolveNegationFunctionName("isCool") }
            }
        }

        @Test
        fun `WITHOUT template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "WITH",
                    negationTemplate = "WITHOUT"
                )

                expect { "withoutMonthly" }
                whenever { config.resolveNegationFunctionName("withMonthly") }
            }
        }

        @Test
        fun `null template returns null`() = test {
            given {
                val config = BooleanAccessorConfig(negationTemplate = null)

                expect { null }
                whenever { config.resolveNegationFunctionName("enabled") }
            }
        }

        @Test
        fun `explicit function name overrides template`() = test {
            given {
                val config = BooleanAccessorConfig(
                    negationFunctionName = "customNegate",
                    negationTemplate = "NOT"
                )

                expect { "customNegate" }
                whenever { config.resolveNegationFunctionName("cool") }
            }
        }
    }

    @Nested
    inner class SelfNegationBehavior {
        @Test
        fun `SELF negation with WITH valid extracts semantic from paired WITHOUT`() = test {
            given {
                // Property is "withoutMonthly", negation is SELF, valid is WITH
                val config = BooleanAccessorConfig(
                    validTemplate = "WITH",
                    negationTemplate = "SELF"
                )

                expect { "withoutMonthly" }
                whenever { config.resolveNegationFunctionName("withoutMonthly") }
            }
        }

        @Test
        fun `SELF negation with WITH valid generates correct valid name`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "WITH",
                    negationTemplate = "SELF"
                )

                expect { "withMonthly" }
                whenever { config.resolveValidFunctionName("withoutMonthly") }
            }
        }
    }

    @Nested
    inner class ExtractSemanticName {
        @Test
        fun `strips known prefix from property name`() = test {
            given {
                expect { "Cool" }
                whenever { BooleanAccessorConfig.extractSemanticName("isCool", "IS", isNegation = false) }
            }
        }

        @Test
        fun `strips negation prefix`() = test {
            given {
                expect { "Monthly" }
                whenever { BooleanAccessorConfig.extractSemanticName("withoutMonthly", "WITHOUT", isNegation = true) }
            }
        }

        @Test
        fun `capitalizes property name when no prefix matches`() = test {
            given {
                expect { "Enabled" }
                whenever { BooleanAccessorConfig.extractSemanticName("enabled", "IS", isNegation = false) }
            }
        }
    }
}

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
    inner class ResolveSemanticNameBranches {
        @Test
        fun `valid SELF with named negation resolves via paired valid`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "SELF",
                    negationTemplate = "IS_NOT"
                )
                // valid=SELF, negation=IS_NOT → resolve via paired(IS_NOT)=IS
                expect { "isNotCool" }
                whenever { config.resolveNegationFunctionName("isCool") }
            }
        }

        @Test
        fun `standalone negation template without valid template`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = null,
                    negationTemplate = "NOT"
                )
                // No valid template, negation is named → extract from negation
                expect { "notEnabled" }
                whenever { config.resolveNegationFunctionName("enabled") }
            }
        }

        @Test
        fun `both templates null falls back to capitalized name`() = test {
            given {
                // resolveValidFunctionName with null template returns propName directly
                // but internally resolveSemanticName won't be called for null template
                val config = BooleanAccessorConfig(
                    validTemplate = "SELF",
                    negationTemplate = "SELF"
                )
                // Both SELF → resolveSemanticName fallback to capitalize
                expect { "enabled" }
                whenever { config.resolveNegationFunctionName("enabled") }
            }
        }

        @Test
        fun `standalone valid template resolves semantic from valid pattern`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "HAS",
                    negationTemplate = "LACKS"
                )
                expect { "hasPermission" }
                whenever { config.resolveValidFunctionName("permission") }
            }
        }

        @Test
        fun `standalone negation named with NONE valid`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "NONE",
                    negationTemplate = "NOT"
                )
                expect { "notEnabled" }
                whenever { config.resolveNegationFunctionName("enabled") }
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

        @Test
        fun `null template capitalizes property name`() = test {
            given {
                expect { "Foo" }
                whenever { BooleanAccessorConfig.extractSemanticName("foo", null, isNegation = false) }
            }
        }

        @Test
        fun `prefix matches but remainder is empty returns capitalized name`() = test {
            given {
                // "is" property with IS template → prefix="is", remainder=""
                expect { "Is" }
                whenever { BooleanAccessorConfig.extractSemanticName("is", "IS", isNegation = false) }
            }
        }

        @Test
        fun `prefix matches but remainder empty returns capitalized name`() = test {
            given {
                // "is" with IS template → prefix="is", remainder="" → fallback
                expect { "Is" }
                whenever { BooleanAccessorConfig.extractSemanticName("is", "IS", isNegation = false) }
            }
        }

        @Test
        fun `negation prefix matches but remainder empty`() = test {
            given {
                expect { "Not" }
                whenever { BooleanAccessorConfig.extractSemanticName("not", "NOT", isNegation = true) }
            }
        }

        @Test
        fun `unknown template name falls back to capitalize`() = test {
            given {
                expect { "Prop" }
                whenever { BooleanAccessorConfig.extractSemanticName("prop", "UNKNOWN", isNegation = false) }
            }
        }
    }

    @Nested
    inner class ApplyTemplate {
        @Test
        fun `unknown template returns null`() = test {
            given {
                expect { null }
                whenever { BooleanAccessorConfig.applyTemplate("UNKNOWN", "Foo", isNegation = false) }
            }
        }

        @Test
        fun `valid template applies correctly`() = test {
            given {
                expect { "isFoo" }
                whenever { BooleanAccessorConfig.applyTemplate("IS", "Foo", isNegation = false) }
            }
        }

        @Test
        fun `negation template applies correctly`() = test {
            given {
                expect { "isNotFoo" }
                whenever { BooleanAccessorConfig.applyTemplate("IS_NOT", "Foo", isNegation = true) }
            }
        }
    }

    @Nested
    inner class PairedTemplate {
        @Test
        fun `valid to negation pair`() = test {
            given {
                expect { "IS_NOT" }
                whenever { BooleanAccessorConfig.pairedTemplate("IS", isNegation = false) }
            }
        }

        @Test
        fun `negation to valid pair`() = test {
            given {
                expect { "IS" }
                whenever { BooleanAccessorConfig.pairedTemplate("IS_NOT", isNegation = true) }
            }
        }

        @Test
        fun `unknown template returns null`() = test {
            given {
                expect { null }
                whenever { BooleanAccessorConfig.pairedTemplate("UNKNOWN", isNegation = false) }
            }
        }
    }

    @Nested
    inner class AllTemplatePatterns {
        @Test
        fun `CAN valid template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(validTemplate = "CAN")
                expect { "canEdit" }
                whenever { config.resolveValidFunctionName("edit") }
            }
        }

        @Test
        fun `SHOULD valid template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(validTemplate = "SHOULD")
                expect { "shouldRun" }
                whenever { config.resolveValidFunctionName("run") }
            }
        }

        @Test
        fun `ENABLE valid template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(validTemplate = "ENABLE")
                expect { "enableFeature" }
                whenever { config.resolveValidFunctionName("feature") }
            }
        }

        @Test
        fun `DISABLE negation template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "ENABLE",
                    negationTemplate = "DISABLE"
                )
                expect { "disableFeature" }
                whenever { config.resolveNegationFunctionName("enableFeature") }
            }
        }

        @Test
        fun `CANNOT negation template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "CAN",
                    negationTemplate = "CANNOT"
                )
                expect { "cannotEdit" }
                whenever { config.resolveNegationFunctionName("canEdit") }
            }
        }

        @Test
        fun `DOES valid with DOES_NOT negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "DOES",
                    negationTemplate = "DOES_NOT"
                )
                expect { "doesNotRun" }
                whenever { config.resolveNegationFunctionName("doesRun") }
            }
        }

        @Test
        fun `HAS valid with LACKS negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "HAS",
                    negationTemplate = "LACKS"
                )
                expect { "lacksPermission" }
                whenever { config.resolveNegationFunctionName("hasPermission") }
            }
        }

        @Test
        fun `WILL valid with WILL_NOT negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "WILL",
                    negationTemplate = "WILL_NOT"
                )
                expect { "willNotExecute" }
                whenever { config.resolveNegationFunctionName("willExecute") }
            }
        }

        @Test
        fun `ALLOW valid with DENY negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "ALLOW",
                    negationTemplate = "DENY"
                )
                expect { "denyAccess" }
                whenever { config.resolveNegationFunctionName("allowAccess") }
            }
        }

        @Test
        fun `SHOULD_NOT negation template`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "SHOULD",
                    negationTemplate = "SHOULD_NOT"
                )
                expect { "shouldNotRun" }
                whenever { config.resolveNegationFunctionName("shouldRun") }
            }
        }
    }

    @Nested
    inner class ValidFunctionWithNegationOnly {
        @Test
        fun `NONE valid with named negation generates only negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "NONE",
                    negationTemplate = "IS_NOT"
                )
                expect { null }
                whenever { config.resolveValidFunctionName("cool") }
            }
        }

        @Test
        fun `explicit valid function name with blank string uses template`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validFunctionName = "",
                    validTemplate = "IS"
                )
                // blank validFunctionName is treated as not set
                expect { "isCool" }
                whenever { config.resolveValidFunctionName("cool") }
            }
        }

        @Test
        fun `explicit negation function name with blank string uses template`() = test {
            given {
                val config = BooleanAccessorConfig(
                    negationFunctionName = "",
                    negationTemplate = "NOT"
                )
                expect { "notCool" }
                whenever { config.resolveNegationFunctionName("cool") }
            }
        }
    }
}

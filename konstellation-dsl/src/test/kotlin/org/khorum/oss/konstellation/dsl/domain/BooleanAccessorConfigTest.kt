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
        fun `standalone DOES_NOT_HAVE negation strips has prefix from property name`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = null,
                    negationTemplate = "DOES_NOT_HAVE"
                )
                // hasTouch → should strip "has" prefix → "doesNotHaveTouch" not "doesNotHaveHasTouch"
                expect { "doesNotHaveTouch" }
                whenever { config.resolveNegationFunctionName("hasTouch") }
            }
        }

        @Test
        fun `standalone IS_NOT negation strips is prefix from property name`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = null,
                    negationTemplate = "IS_NOT"
                )
                // isVisible → should strip "is" prefix → "isNotVisible"
                expect { "isNotVisible" }
                whenever { config.resolveNegationFunctionName("isVisible") }
            }
        }

        @Test
        fun `SELF valid with DOES_NOT_HAVE negation strips has prefix`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "SELF",
                    negationTemplate = "DOES_NOT_HAVE"
                )
                // Simulates KSP default validTemplate=SELF with user-set negation
                expect { "doesNotHaveTouch" }
                whenever { config.resolveNegationFunctionName("hasTouch") }
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
        fun `EXISTS valid to ABSENT negation pair`() = test {
            given {
                expect { "ABSENT" }
                whenever { BooleanAccessorConfig.pairedTemplate("EXISTS", isNegation = false) }
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
        fun `ENABLED valid template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(validTemplate = "ENABLED")
                expect { "enabledModule" }
                whenever { config.resolveValidFunctionName("module") }
            }
        }

        @Test
        fun `IS_ENABLED valid template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(validTemplate = "IS_ENABLED")
                expect { "isEnabledService" }
                whenever { config.resolveValidFunctionName("service") }
            }
        }

        @Test
        fun `PRESENT valid template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(validTemplate = "PRESENT")
                expect { "presentData" }
                whenever { config.resolveValidFunctionName("data") }
            }
        }

        @Test
        fun `IS_PRESENT valid template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(validTemplate = "IS_PRESENT")
                expect { "isPresentFeature" }
                whenever { config.resolveValidFunctionName("feature") }
            }
        }

        @Test
        fun `ALWAYS valid template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(validTemplate = "ALWAYS")
                expect { "alwaysRetry" }
                whenever { config.resolveValidFunctionName("retry") }
            }
        }

        @Test
        fun `EXISTS valid template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(validTemplate = "EXISTS")
                expect { "existsRecord" }
                whenever { config.resolveValidFunctionName("record") }
            }
        }

        @Test
        fun `DISABLED negation template applies pattern`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "ENABLED",
                    negationTemplate = "DISABLED"
                )
                expect { "disabledModule" }
                whenever { config.resolveNegationFunctionName("enabledModule") }
            }
        }

        @Test
        fun `IS_DISABLED negation template`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "IS_ENABLED",
                    negationTemplate = "IS_DISABLED"
                )
                expect { "isDisabledService" }
                whenever { config.resolveNegationFunctionName("isEnabledService") }
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
        fun `HAS valid with DOES_NOT_HAVE negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "HAS",
                    negationTemplate = "DOES_NOT_HAVE"
                )
                expect { "doesNotHaveAuth" }
                whenever { config.resolveNegationFunctionName("hasAuth") }
            }
        }

        @Test
        fun `HAS valid with HAS_NOT negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "HAS",
                    negationTemplate = "HAS_NOT"
                )
                expect { "hasNotLicense" }
                whenever { config.resolveNegationFunctionName("hasLicense") }
            }
        }

        @Test
        fun `ALWAYS valid with NEVER negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "ALWAYS",
                    negationTemplate = "NEVER"
                )
                expect { "neverRetry" }
                whenever { config.resolveNegationFunctionName("alwaysRetry") }
            }
        }

        @Test
        fun `IS valid with MISSING negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "IS",
                    negationTemplate = "MISSING"
                )
                expect { "missingVisible" }
                whenever { config.resolveNegationFunctionName("isVisible") }
            }
        }

        @Test
        fun `IS valid with IS_MISSING negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "IS",
                    negationTemplate = "IS_MISSING"
                )
                expect { "isMissingAvailable" }
                whenever { config.resolveNegationFunctionName("isAvailable") }
            }
        }

        @Test
        fun `PRESENT valid with ABSENT negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "PRESENT",
                    negationTemplate = "ABSENT"
                )
                expect { "absentData" }
                whenever { config.resolveNegationFunctionName("presentData") }
            }
        }

        @Test
        fun `IS_PRESENT valid with IS_ABSENT negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "IS_PRESENT",
                    negationTemplate = "IS_ABSENT"
                )
                expect { "isAbsentFeature" }
                whenever { config.resolveNegationFunctionName("isPresentFeature") }
            }
        }

        @Test
        fun `DOES valid with NO negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "DOES",
                    negationTemplate = "NO"
                )
                expect { "noSync" }
                whenever { config.resolveNegationFunctionName("doesSync") }
            }
        }

        @Test
        fun `DOES valid with DO_NOT negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "DOES",
                    negationTemplate = "DO_NOT"
                )
                expect { "doNotRun" }
                whenever { config.resolveNegationFunctionName("doesRun") }
            }
        }

        @Test
        fun `EXISTS valid with ABSENT negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "EXISTS",
                    negationTemplate = "ABSENT"
                )
                expect { "absentRecord" }
                whenever { config.resolveNegationFunctionName("existsRecord") }
            }
        }

        @Test
        fun `IS_ENABLED valid with IS_DENIED negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "IS_ENABLED",
                    negationTemplate = "IS_DENIED"
                )
                expect { "isDeniedAccess" }
                whenever { config.resolveNegationFunctionName("isEnabledAccess") }
            }
        }

        @Test
        fun `SELF valid with DENY negation`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "SELF",
                    negationTemplate = "DENY"
                )
                expect { "denyAccess" }
                whenever { config.resolveNegationFunctionName("access") }
            }
        }
    }

    @Nested
    inner class AutoDetectPrefixStripping {
        @Test
        fun `has prefix with DOES_NOT_HAVE negation`() = test {
            given {
                expect { "doesNotHaveTouch" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("hasTouch", "DOES_NOT_HAVE") }
            }
        }

        @Test
        fun `is prefix with IS_NOT negation`() = test {
            given {
                expect { "isNotVisible" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("isVisible", "IS_NOT") }
            }
        }

        @Test
        fun `does prefix with DOES_NOT negation`() = test {
            given {
                expect { "doesNotSync" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("doesSync", "DOES_NOT") }
            }
        }

        @Test
        fun `with prefix with WITHOUT negation`() = test {
            given {
                expect { "withoutCache" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("withCache", "WITHOUT") }
            }
        }

        @Test
        fun `has prefix with LACKS negation`() = test {
            given {
                expect { "lacksPermission" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("hasPermission", "LACKS") }
            }
        }

        @Test
        fun `has prefix with HAS_NOT negation`() = test {
            given {
                expect { "hasNotPermission" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("hasPermission", "HAS_NOT") }
            }
        }

        @Test
        fun `enabled prefix with DISABLED negation`() = test {
            given {
                expect { "disabledModule" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("enabledModule", "DISABLED") }
            }
        }

        @Test
        fun `isEnabled prefix with IS_DISABLED negation`() = test {
            given {
                expect { "isDisabledService" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("isEnabledService", "IS_DISABLED") }
            }
        }

        @Test
        fun `always prefix with NEVER negation`() = test {
            given {
                expect { "neverRetry" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("alwaysRetry", "NEVER") }
            }
        }

        @Test
        fun `present prefix with ABSENT negation`() = test {
            given {
                expect { "absentData" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("presentData", "ABSENT") }
            }
        }

        @Test
        fun `isPresent prefix with IS_ABSENT negation`() = test {
            given {
                expect { "isAbsentFeature" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("isPresentFeature", "IS_ABSENT") }
            }
        }

        @Test
        fun `does prefix with DO_NOT negation`() = test {
            given {
                expect { "doNotRun" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("doesRun", "DO_NOT") }
            }
        }

        @Test
        fun `exists prefix with ABSENT negation`() = test {
            given {
                expect { "absentRecord" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("existsRecord", "ABSENT") }
            }
        }

        @Test
        fun `isEnabled prefix with IS_DENIED negation`() = test {
            given {
                expect { "isDeniedAccess" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("isEnabledAccess", "IS_DENIED") }
            }
        }

        @Test
        fun `DENY does not strip any prefix - preserves full name`() = test {
            given {
                // DENY has no semantically related valid template to strip
                expect { "denyAccess" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("access", "DENY") }
            }
        }

        @Test
        fun `NOT does not strip is prefix - preserves full name`() = test {
            given {
                // NOT has no semantically related valid template to strip
                expect { "notIsCool" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("isCool", "NOT") }
            }
        }

        @Test
        fun `NO does not strip does prefix - preserves full name`() = test {
            given {
                expect { "noDoesSync" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("doesSync", "NO") }
            }
        }

        @Test
        fun `no known prefix falls back to prefix application`() = test {
            given {
                expect { "notCool" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("cool", "NOT") }
            }
        }
    }

    @Nested
    inner class AutoDetectSuffixStripping {
        @Test
        fun `Enabled suffix with DISABLED negation`() = test {
            given {
                // someItemEnabled → strip "Enabled" suffix → "someItem" + "Disabled"
                expect { "someItemDisabled" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("someItemEnabled", "DISABLED") }
            }
        }

        @Test
        fun `IsEnabled suffix with IS_DISABLED negation`() = test {
            given {
                expect { "myServiceIsDisabled" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("myServiceIsEnabled", "IS_DISABLED") }
            }
        }

        @Test
        fun `Has suffix with LACKS negation`() = test {
            given {
                expect { "itemLacks" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("itemHas", "LACKS") }
            }
        }

        @Test
        fun `Has suffix with DOES_NOT_HAVE negation`() = test {
            given {
                expect { "touchDoesNotHave" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("touchHas", "DOES_NOT_HAVE") }
            }
        }

        @Test
        fun `With suffix with WITHOUT negation`() = test {
            given {
                expect { "cacheWithout" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("cacheWith", "WITHOUT") }
            }
        }

        @Test
        fun `Present suffix with ABSENT negation`() = test {
            given {
                expect { "dataAbsent" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("dataPresent", "ABSENT") }
            }
        }

        @Test
        fun `IsPresent suffix with IS_ABSENT negation`() = test {
            given {
                expect { "featureIsAbsent" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("featureIsPresent", "IS_ABSENT") }
            }
        }

        @Test
        fun `Always suffix with NEVER negation`() = test {
            given {
                expect { "retryNever" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("retryAlways", "NEVER") }
            }
        }

        @Test
        fun `Exists suffix with ABSENT negation`() = test {
            given {
                expect { "recordAbsent" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("recordExists", "ABSENT") }
            }
        }

        @Test
        fun `IsEnabled suffix with IS_DENIED negation`() = test {
            given {
                expect { "accessIsDenied" }
                whenever { BooleanAccessorConfig.resolveNegationByAutoDetect("accessIsEnabled", "IS_DENIED") }
            }
        }
    }

    @Nested
    inner class AutoDetectEndToEnd {
        @Test
        fun `SELF valid + DISABLED negation with suffix property`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "SELF",
                    negationTemplate = "DISABLED"
                )
                expect { "someItemDisabled" }
                whenever { config.resolveNegationFunctionName("someItemEnabled") }
            }
        }

        @Test
        fun `SELF valid + DISABLED negation with prefix property`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "SELF",
                    negationTemplate = "DISABLED"
                )
                expect { "disabledModule" }
                whenever { config.resolveNegationFunctionName("enabledModule") }
            }
        }

        @Test
        fun `null valid + DISABLED negation with suffix property`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = null,
                    negationTemplate = "DISABLED"
                )
                expect { "someItemDisabled" }
                whenever { config.resolveNegationFunctionName("someItemEnabled") }
            }
        }

        @Test
        fun `null valid + LACKS negation with has prefix property`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = null,
                    negationTemplate = "LACKS"
                )
                expect { "lacksPermission" }
                whenever { config.resolveNegationFunctionName("hasPermission") }
            }
        }

        @Test
        fun `SELF valid + WITHOUT negation with with prefix property`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "SELF",
                    negationTemplate = "WITHOUT"
                )
                expect { "withoutCache" }
                whenever { config.resolveNegationFunctionName("withCache") }
            }
        }

        @Test
        fun `SELF valid + NEVER negation with always prefix property`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "SELF",
                    negationTemplate = "NEVER"
                )
                expect { "neverRetry" }
                whenever { config.resolveNegationFunctionName("alwaysRetry") }
            }
        }

        @Test
        fun `SELF valid + NOT negation with plain property`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "SELF",
                    negationTemplate = "NOT"
                )
                // "logging" has no known prefix/suffix — apply NOT as prefix
                expect { "notLogging" }
                whenever { config.resolveNegationFunctionName("logging") }
            }
        }
    }

    @Nested
    inner class ValidFunctionPreservation {
        @Test
        fun `SELF valid preserved when negation is DOES_NOT_HAVE`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "SELF",
                    negationTemplate = "DOES_NOT_HAVE"
                )
                expect { "hasTouch" }
                whenever { config.resolveValidFunctionName("hasTouch") }
            }
        }

        @Test
        fun `SELF valid preserved when negation is DISABLED`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "SELF",
                    negationTemplate = "DISABLED"
                )
                expect { "someItemEnabled" }
                whenever { config.resolveValidFunctionName("someItemEnabled") }
            }
        }

        @Test
        fun `null valid preserved when negation is set`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = null,
                    negationTemplate = "NOT"
                )
                // null validTemplate → returns propName (not removed)
                expect { "enabled" }
                whenever { config.resolveValidFunctionName("enabled") }
            }
        }

        @Test
        fun `NONE valid explicitly suppresses valid function`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "NONE",
                    negationTemplate = "NOT"
                )
                expect { null }
                whenever { config.resolveValidFunctionName("enabled") }
            }
        }

        @Test
        fun `valid function only removed when negation is SELF and no valid override`() = test {
            given {
                // This simulates the PropertySchemaService blanking logic
                val config = BooleanAccessorConfig(
                    validTemplate = "NONE", // set by PropertySchemaService when negation=SELF
                    negationTemplate = "SELF"
                )
                expect { null }
                whenever { config.resolveValidFunctionName("blocked") }
            }
        }

        @Test
        fun `valid function kept when negation is SELF but valid is explicitly set`() = test {
            given {
                val config = BooleanAccessorConfig(
                    validTemplate = "IS",
                    negationTemplate = "SELF"
                )
                expect { "isBlocked" }
                whenever { config.resolveValidFunctionName("blocked") }
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

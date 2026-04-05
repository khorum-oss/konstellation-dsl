package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.geordi.UnitSim
import kotlin.test.Test

/**
 * Integration tests for every boolean template combination in [FeatureFlags].
 */
class FeatureFlagsTest : UnitSim() {

    // ── 1. Inferred defaults (@DefaultTrue → SELF + NOT) ───────────────

    @Test
    fun `inferred default - logging() sets true, notLogging() sets false`() = test {
        given {
            expect { true }
            whenever {
                val flags = featureFlags { logging(); notLogging(); logging() }
                flags.logging
            }
        }
    }

    @Test
    fun `inferred default - tracing() sets true, notTracing() negates`() = test {
        given {
            expect { false }
            whenever { featureFlags { notTracing() }.tracing }
        }
    }

    @Test
    fun `inferred default - notTracing with false double-negates to true`() = test {
        given {
            expect { true }
            whenever { featureFlags { notTracing(false) }.tracing }
        }
    }

    // ── 2. SELF valid + explicit negation ───────────────────────────────

    @Test
    fun `SELF + IS_NOT - isEnabled() and isNotEnabled()`() = test {
        given {
            expect { true }
            whenever {
                val f = featureFlags { isEnabled() }
                f.isEnabled && featureFlags { isNotEnabled() }.isEnabled == false
            }
        }
    }

    @Test
    fun `SELF + WITHOUT - withCache() and withoutCache()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { withCache() }.withCache &&
                    !featureFlags { withoutCache() }.withCache
            }
        }
    }

    // ── 3. Explicit valid + paired negation ─────────────────────────────

    @Test
    fun `IS + IS_NOT - isActive() and isNotActive()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { isActive() }.active &&
                    !featureFlags { isNotActive() }.active
            }
        }
    }

    @Test
    fun `HAS + LACKS - hasPermission() and lacksPermission()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { hasPermission() }.permission &&
                    !featureFlags { lacksPermission() }.permission
            }
        }
    }

    @Test
    fun `DOES + DOES_NOT - doesValidate() and doesNotValidate()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { doesValidate() }.validate &&
                    !featureFlags { doesNotValidate() }.validate
            }
        }
    }

    @Test
    fun `WITH + WITHOUT - withCompression() and withoutCompression()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { withCompression() }.compression &&
                    !featureFlags { withoutCompression() }.compression
            }
        }
    }

    // ── 4. NONE suppression ─────────────────────────────────────────────

    @Test
    fun `IS + NONE - only isReady() exists (no negation)`() = test {
        given {
            expect { true }
            whenever { featureFlags { isReady() }.ready }
        }
    }

    @Test
    fun `IS + NONE - isReady(false) disables`() = test {
        given {
            expect { false }
            whenever { featureFlags { isReady(false) }.ready }
        }
    }

    @Test
    fun `NONE + NOT - only notBlocked() exists (no valid)`() = test {
        given {
            expect { true }
            whenever {
                // notBlocked() sets blocked = !on, so notBlocked(true) → blocked = false
                // Default is false, notBlocked() makes it !true = false (same)
                // notBlocked(false) makes it !false = true
                featureFlags { notBlocked(false) }.blocked
            }
        }
    }

    @Test
    fun `NONE + NOT - notBlocked() keeps blocked false`() = test {
        given {
            expect { false }
            whenever { featureFlags { notBlocked() }.blocked }
        }
    }

    // ── 5. Custom function names ────────────────────────────────────────

    @Test
    fun `custom names - turnOn() and turnOff()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { turnOn() }.power &&
                    !featureFlags { turnOff() }.power
            }
        }
    }

    @Test
    fun `mixed custom + template - activate() and isNotShield()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { activate() }.shield &&
                    !featureFlags { isNotShield() }.shield
            }
        }
    }

    // ── 6. Additional meta-dsl 1.0.12 templates ────────────────────────

    @Test
    fun `ALWAYS + NEVER - alwaysRetry() and neverRetry()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { alwaysRetry() }.retry &&
                    !featureFlags { neverRetry() }.retry
            }
        }
    }

    @Test
    fun `IS_PRESENT + IS_ABSENT - isPresentFeatureX() and isAbsentFeatureX()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { isPresentFeatureX() }.featureX &&
                    !featureFlags { isAbsentFeatureX() }.featureX
            }
        }
    }

    @Test
    fun `PRESENT + ABSENT - presentData() and absentData()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { presentData() }.data &&
                    !featureFlags { absentData() }.data
            }
        }
    }

    @Test
    fun `ENABLED + DISABLED - enabledModule() and disabledModule()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { enabledModule() }.module &&
                    !featureFlags { disabledModule() }.module
            }
        }
    }

    @Test
    fun `IS_ENABLED + IS_DISABLED - isEnabledService() and isDisabledService()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { isEnabledService() }.service &&
                    !featureFlags { isDisabledService() }.service
            }
        }
    }

    @Test
    fun `HAS + DOES_NOT_HAVE - hasAuth() and doesNotHaveAuth()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { hasAuth() }.auth &&
                    !featureFlags { doesNotHaveAuth() }.auth
            }
        }
    }

    @Test
    fun `HAS + HAS_NOT - hasLicense() and hasNotLicense()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { hasLicense() }.license &&
                    !featureFlags { hasNotLicense() }.license
            }
        }
    }

    @Test
    fun `IS + MISSING - isVisible() and missingVisible()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { isVisible() }.visible &&
                    !featureFlags { missingVisible() }.visible
            }
        }
    }

    @Test
    fun `IS + IS_MISSING - isAvailable() and isMissingAvailable()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { isAvailable() }.available &&
                    !featureFlags { isMissingAvailable() }.available
            }
        }
    }

    @Test
    fun `DOES + NO - doesSync() and noSync()`() = test {
        given {
            expect { true }
            whenever {
                featureFlags { doesSync() }.sync &&
                    !featureFlags { noSync() }.sync
            }
        }
    }

    // ── Full object construction ────────────────────────────────────────

    @Test
    fun `all defaults produce expected object`() = test {
        given {
            expect {
                FeatureFlags(
                    configName = "",
                    logging = true, tracing = false,
                    isEnabled = true, withCache = true,
                    active = true, permission = false, validate = true, compression = false,
                    ready = true, blocked = false,
                    power = false, shield = true,
                    retry = false, featureX = false, data = true,
                    module = false, service = true,
                    auth = false, license = true,
                    visible = true, available = true, sync = false
                )
            }
            whenever { featureFlags { } }
        }
    }

    @Test
    fun `all flags flipped via templates`() = test {
        given {
            expect {
                FeatureFlags(
                    configName = "flipped",
                    logging = false, tracing = true,
                    isEnabled = false, withCache = false,
                    active = false, permission = true, validate = false, compression = true,
                    ready = false, blocked = true,
                    power = true, shield = false,
                    retry = true, featureX = true, data = false,
                    module = true, service = false,
                    auth = true, license = false,
                    visible = false, available = false, sync = true
                )
            }
            whenever {
                featureFlags {
                    configName = "flipped"
                    notLogging(); tracing()
                    isNotEnabled(); withoutCache()
                    isNotActive(); hasPermission(); doesNotValidate(); withCompression()
                    isReady(false); notBlocked(false)
                    turnOn(); isNotShield()
                    alwaysRetry(); isPresentFeatureX(); absentData()
                    enabledModule(); isDisabledService()
                    hasAuth(); hasNotLicense()
                    missingVisible(); isMissingAvailable(); doesSync()
                }
            }
        }
    }
}

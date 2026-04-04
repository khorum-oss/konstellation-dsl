package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.geordi.UnitSim
import kotlin.test.Test

class SecurityPolicyTest : UnitSim() {

    // ── Defaults ────────────────────────────────────────────────────────

    @Test
    fun `empty builder produces all defaults`() = test {
        given {
            expect {
                SecurityPolicy(
                    policyName = "",
                    firewallEnabled = true,
                    auditLogging = false,
                    encryptionEnabled = true,
                    rateLimitingEnabled = false,
                    intrusionDetection = true,
                    twoFactorAuth = false,
                    mfaRequired = false,
                    debugMode = null,
                    maintenanceMode = null,
                    port = 0,
                    maxRetries = 0,
                    timeoutSeconds = 0
                )
            }
            whenever { securityPolicy { } }
        }
    }

    // ── @DefaultTrue fields ─────────────────────────────────────────────

    @Test
    fun `@DefaultTrue firewallEnabled defaults to true`() = test {
        given {
            expect { true }
            whenever { securityPolicy { }.firewallEnabled }
        }
    }

    @Test
    fun `@DefaultTrue firewallEnabled can be disabled`() = test {
        given {
            expect { false }
            whenever { securityPolicy { firewallEnabled(false) }.firewallEnabled }
        }
    }

    @Test
    fun `@DefaultTrue firewallEnabled no-arg call keeps true`() = test {
        given {
            expect { true }
            whenever { securityPolicy { firewallEnabled() }.firewallEnabled }
        }
    }

    @Test
    fun `@DefaultTrue encryptionEnabled defaults to true`() = test {
        given {
            expect { true }
            whenever { securityPolicy { }.encryptionEnabled }
        }
    }

    @Test
    fun `@DefaultTrue encryptionEnabled can be disabled`() = test {
        given {
            expect { false }
            whenever { securityPolicy { encryptionEnabled(false) }.encryptionEnabled }
        }
    }

    @Test
    fun `@DefaultTrue intrusionDetection defaults to true`() = test {
        given {
            expect { true }
            whenever { securityPolicy { }.intrusionDetection }
        }
    }

    @Test
    fun `@DefaultTrue intrusionDetection can be disabled`() = test {
        given {
            expect { false }
            whenever { securityPolicy { intrusionDetection(false) }.intrusionDetection }
        }
    }

    // ── @DefaultFalse fields ────────────────────────────────────────────

    @Test
    fun `@DefaultFalse auditLogging defaults to false`() = test {
        given {
            expect { false }
            whenever { securityPolicy { }.auditLogging }
        }
    }

    @Test
    fun `@DefaultFalse auditLogging can be enabled`() = test {
        given {
            expect { true }
            whenever { securityPolicy { auditLogging(true) }.auditLogging }
        }
    }

    @Test
    fun `@DefaultFalse auditLogging no-arg call keeps false`() = test {
        given {
            expect { false }
            whenever { securityPolicy { auditLogging() }.auditLogging }
        }
    }

    @Test
    fun `@DefaultFalse rateLimitingEnabled defaults to false`() = test {
        given {
            expect { false }
            whenever { securityPolicy { }.rateLimitingEnabled }
        }
    }

    @Test
    fun `@DefaultFalse rateLimitingEnabled can be enabled`() = test {
        given {
            expect { true }
            whenever { securityPolicy { rateLimitingEnabled(true) }.rateLimitingEnabled }
        }
    }

    @Test
    fun `@DefaultFalse mfaRequired defaults to false`() = test {
        given {
            expect { false }
            whenever { securityPolicy { }.mfaRequired }
        }
    }

    @Test
    fun `@DefaultFalse mfaRequired can be enabled`() = test {
        given {
            expect { true }
            whenever { securityPolicy { mfaRequired(true) }.mfaRequired }
        }
    }

    // ── Nullable booleans (no default annotation) ───────────────────────

    @Test
    fun `nullable debugMode defaults to null`() = test {
        given {
            expect { null }
            whenever { securityPolicy { }.debugMode }
        }
    }

    @Test
    fun `nullable debugMode can be set to true`() = test {
        given {
            expect { true }
            whenever { securityPolicy { debugMode(true) }.debugMode }
        }
    }

    @Test
    fun `nullable debugMode can be set to false`() = test {
        given {
            expect { false }
            whenever { securityPolicy { debugMode(false) }.debugMode }
        }
    }

    @Test
    fun `nullable debugMode no-arg defaults to true`() = test {
        given {
            expect { true }
            whenever { securityPolicy { debugMode() }.debugMode }
        }
    }

    @Test
    fun `nullable maintenanceMode defaults to null`() = test {
        given {
            expect { null }
            whenever { securityPolicy { }.maintenanceMode }
        }
    }

    @Test
    fun `nullable maintenanceMode can be toggled`() = test {
        given {
            expect { false }
            whenever {
                securityPolicy {
                    maintenanceMode(true)
                    maintenanceMode(false)
                }.maintenanceMode
            }
        }
    }

    // ── @DslAlias on boolean ────────────────────────────────────────────

    @Test
    fun `ids alias sets intrusionDetection to true`() = test {
        given {
            expect { true }
            whenever { securityPolicy { ids() }.intrusionDetection }
        }
    }

    @Test
    fun `ids alias can disable intrusionDetection`() = test {
        given {
            expect { false }
            whenever { securityPolicy { ids(false) }.intrusionDetection }
        }
    }

    @Test
    fun `intrusionDetection and ids alias are interchangeable`() = test {
        given {
            expect { false }
            whenever {
                securityPolicy {
                    intrusionDetection(true)
                    ids(false) // alias overrides
                }.intrusionDetection
            }
        }
    }

    // ── @DeprecatedDsl on boolean ───────────────────────────────────────

    @Test
    fun `deprecated twoFactorAuth still works`() = test {
        given {
            expect { true }
            @Suppress("DEPRECATION")
            whenever { securityPolicy { twoFactorAuth(true) }.twoFactorAuth }
        }
    }

    @Test
    fun `deprecated twoFactorAuth defaults to false`() = test {
        given {
            expect { false }
            whenever { securityPolicy { }.twoFactorAuth }
        }
    }

    @Test
    fun `mfaRequired replaces twoFactorAuth`() = test {
        given {
            expect { true }
            whenever {
                val policy = securityPolicy { mfaRequired(true) }
                policy.mfaRequired && !policy.twoFactorAuth
            }
        }
    }

    // ── @ValidateDsl ────────────────────────────────────────────────────

    @Test
    fun `port validation rejects negative`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>("port must be 0-65535") {
                securityPolicy { port = -1 }
            }
        }
    }

    @Test
    fun `port validation rejects too high`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>("port must be 0-65535") {
                securityPolicy { port = 70000 }
            }
        }
    }

    @Test
    fun `port validation accepts valid port`() = test {
        given {
            expect { 8080 }
            whenever { securityPolicy { port = 8080 }.port }
        }
    }

    @Test
    fun `port validation accepts zero`() = test {
        given {
            expect { 0 }
            whenever { securityPolicy { }.port }
        }
    }

    @Test
    fun `port validation accepts max port 65535`() = test {
        given {
            expect { 65535 }
            whenever { securityPolicy { port = 65535 }.port }
        }
    }

    @Test
    fun `maxRetries validation rejects negative`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>("maxRetries must be non-negative") {
                securityPolicy { maxRetries = -1 }
            }
        }
    }

    @Test
    fun `maxRetries validation accepts zero`() = test {
        given {
            expect { 0 }
            whenever { securityPolicy { }.maxRetries }
        }
    }

    @Test
    fun `maxRetries validation accepts positive`() = test {
        given {
            expect { 5 }
            whenever { securityPolicy { maxRetries = 5 }.maxRetries }
        }
    }

    // ── Combined state scenarios ────────────────────────────────────────

    @Test
    fun `all security features enabled`() = test {
        given {
            expect {
                SecurityPolicy(
                    policyName = "max-security",
                    firewallEnabled = true,
                    auditLogging = true,
                    encryptionEnabled = true,
                    rateLimitingEnabled = true,
                    intrusionDetection = true,
                    twoFactorAuth = false,
                    mfaRequired = true,
                    debugMode = false,
                    maintenanceMode = false,
                    port = 443,
                    maxRetries = 3,
                    timeoutSeconds = 30
                )
            }
            whenever {
                securityPolicy {
                    policyName = "max-security"
                    firewallEnabled()
                    auditLogging(true)
                    encryptionEnabled()
                    rateLimitingEnabled(true)
                    ids()
                    mfaRequired(true)
                    debugMode(false)
                    maintenanceMode(false)
                    port = 443
                    maxRetries = 3
                    timeoutSeconds = 30
                }
            }
        }
    }

    @Test
    fun `all security features disabled`() = test {
        given {
            expect {
                SecurityPolicy(
                    policyName = "open-policy",
                    firewallEnabled = false,
                    auditLogging = false,
                    encryptionEnabled = false,
                    rateLimitingEnabled = false,
                    intrusionDetection = false,
                    twoFactorAuth = false,
                    mfaRequired = false,
                    debugMode = true,
                    maintenanceMode = true,
                    port = 8080,
                    maxRetries = 0,
                    timeoutSeconds = 0
                )
            }
            whenever {
                securityPolicy {
                    policyName = "open-policy"
                    firewallEnabled(false)
                    auditLogging(false)
                    encryptionEnabled(false)
                    rateLimitingEnabled(false)
                    intrusionDetection(false)
                    mfaRequired(false)
                    debugMode(true)
                    maintenanceMode(true)
                    port = 8080
                }
            }
        }
    }

    @Test
    fun `toggle sequence - last call wins`() = test {
        given {
            expect { true }
            whenever {
                securityPolicy {
                    firewallEnabled(false)
                    firewallEnabled(true)
                    firewallEnabled(false)
                    firewallEnabled(true)
                }.firewallEnabled
            }
        }
    }

    @Test
    fun `mixed boolean defaults are independent`() = test {
        given {
            expect { true }
            whenever {
                val policy = securityPolicy {
                    auditLogging(true)  // override @DefaultFalse
                }
                // auditLogging overridden, but firewallEnabled still at default
                policy.auditLogging && policy.firewallEnabled
            }
        }
    }

    // ── @DslDescription is present on generated code ────────────────────

    @Test
    fun `policyName can be set`() = test {
        given {
            expect { "zero-trust" }
            whenever { securityPolicy { policyName = "zero-trust" }.policyName }
        }
    }

    @Test
    fun `timeoutSeconds uses default zero`() = test {
        given {
            expect { 0 }
            whenever { securityPolicy { }.timeoutSeconds }
        }
    }

    @Test
    fun `timeoutSeconds can be set`() = test {
        given {
            expect { 60 }
            whenever { securityPolicy { timeoutSeconds = 60 }.timeoutSeconds }
        }
    }
}

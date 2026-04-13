package org.khorum.oss.konstellation.generatetest

import org.khorum.oss.konstellation.metaDsl.annotation.DslAlias
import org.khorum.oss.konstellation.metaDsl.annotation.DslDescription
import org.khorum.oss.konstellation.metaDsl.annotation.DeprecatedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.RootDsl
import org.khorum.oss.konstellation.metaDsl.annotation.ValidateDsl
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultFalse
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultTrue
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.NegationFunctionTemplate
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.ValidFunctionTemplate
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultEmptyString
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultZeroInt

/**
 * Domain class exercising boolean-heavy DSL generation with various annotation combinations.
 *
 * Covers: @DefaultTrue, @DefaultFalse, @DslAlias on booleans, @DeprecatedDsl on booleans,
 * @DslDescription on booleans, @ValidateDsl on non-boolean fields alongside booleans,
 * and nullable vs non-nullable booleans.
 */
@RootDsl
@GeneratedDsl
data class SecurityPolicy(
    @DefaultEmptyString
    val policyName: String,

    // ── Basic boolean defaults ──────────────────────────────────────────

    /** Firewall enabled by default. */
    @DefaultTrue
    @DslDescription("Whether the firewall is active")
    val firewallEnabled: Boolean,

    /** Logging disabled by default. */
    @DefaultFalse
    @DslDescription("Whether audit logging is active")
    val auditLogging: Boolean,

    /** Encryption on by default. */
    @DefaultTrue
    val encryptionEnabled: Boolean,

    /** Rate limiting off by default. */
    @DefaultFalse
    val rateLimitingEnabled: Boolean,

    // ── Boolean with alias ──────────────────────────────────────────────

    /** Intrusion detection with alias "ids" — suppress negation to avoid alias conflict. */
    @DefaultTrue(negationTemplate = NegationFunctionTemplate.NONE)
    @DslAlias(names = ["ids"])
    val intrusionDetection: Boolean,

    // ── Deprecated boolean ──────────────────────────────────────────────

    /** Legacy 2FA flag — replaced by mfaRequired. */
    @DefaultFalse
    @DeprecatedDsl(message = "Use 'mfaRequired' instead", replaceWith = "mfaRequired")
    val twoFactorAuth: Boolean,

    /** Modern MFA flag. */
    @DefaultFalse
    val mfaRequired: Boolean,

    // ── Nullable booleans (no @Default annotation) ──────────────────────

    /** Nullable boolean — not set by default. */
    val debugMode: Boolean? = null,

    /** Another nullable boolean — not set by default. */
    val maintenanceMode: Boolean? = null,

    // ── Non-boolean fields alongside booleans ───────────────────────────

    @DefaultZeroInt
    @ValidateDsl(expression = "it?.let { v -> v in 0..65535 } ?: true", message = "port must be 0-65535")
    val port: Int,

    @DefaultZeroInt
    @ValidateDsl(expression = "it?.let { v -> v >= 0 } ?: true", message = "maxRetries must be non-negative")
    val maxRetries: Int,

    @DefaultZeroInt
    val timeoutSeconds: Int
)

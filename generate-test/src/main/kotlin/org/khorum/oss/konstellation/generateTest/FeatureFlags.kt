package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.RootDsl
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultFalse
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultTrue
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultEmptyString
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.NegationFunctionTemplate
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.ValidFunctionTemplate

/**
 * Demonstrates every boolean template combination available in meta-dsl 1.0.12.
 *
 * Each field shows a different template configuration:
 * - Inferred (default): @DefaultTrue → SELF valid + NOT negation
 * - Explicit templates: each ValidFunctionTemplate / NegationFunctionTemplate
 * - SELF/NONE: suppress one accessor
 * - Custom function names: override via validFunctionName/negationFunctionName
 */
@RootDsl
@GeneratedDsl
data class FeatureFlags(
    @DefaultEmptyString
    val configName: String,

    // ── 1. Inferred default: @DefaultTrue → valid=SELF, negation=NOT ────

    /** Default: property name as-is + not{X} negation. */
    @DefaultTrue
    val logging: Boolean,

    /** Default: property name as-is + not{X} negation. */
    @DefaultFalse
    val tracing: Boolean,

    // ── 2. SELF valid + explicit negation ────────────────────────────────

    /** SELF + IS_NOT: isEnabled() / isNotEnabled(). Semantic name inferred from SELF. */
    @DefaultTrue(
        validTemplate = ValidFunctionTemplate.SELF,
        negationTemplate = NegationFunctionTemplate.IS_NOT
    )
    val isEnabled: Boolean,

    /** SELF + WITHOUT. */
    @DefaultTrue(
        validTemplate = ValidFunctionTemplate.SELF,
        negationTemplate = NegationFunctionTemplate.WITHOUT
    )
    val withCache: Boolean,

    // ── 3. Explicit valid template + paired negation ────────────────────

    /** IS + IS_NOT: isActive() / isNotActive(). */
    @DefaultTrue(
        validTemplate = ValidFunctionTemplate.IS,
        negationTemplate = NegationFunctionTemplate.IS_NOT
    )
    val active: Boolean,

    /** HAS + LACKS: hasPermission() / lacksPermission(). */
    @DefaultFalse(
        validTemplate = ValidFunctionTemplate.HAS,
        negationTemplate = NegationFunctionTemplate.LACKS
    )
    val permission: Boolean,

    /** DOES + DOES_NOT: doesValidate() / doesNotValidate(). */
    @DefaultTrue(
        validTemplate = ValidFunctionTemplate.DOES,
        negationTemplate = NegationFunctionTemplate.DOES_NOT
    )
    val validate: Boolean,

    /** WITH + WITHOUT: withCompression() / withoutCompression(). */
    @DefaultFalse(
        validTemplate = ValidFunctionTemplate.WITH,
        negationTemplate = NegationFunctionTemplate.WITHOUT
    )
    val compression: Boolean,

    // ── 4. Suppress one side with NONE ──────────────────────────────────

    /** Valid only (no negation): isReady(). */
    @DefaultTrue(
        validTemplate = ValidFunctionTemplate.IS,
        negationTemplate = NegationFunctionTemplate.NONE
    )
    val ready: Boolean,

    /** Negation only (no valid): notBlocked(). */
    @DefaultFalse(
        validTemplate = ValidFunctionTemplate.NONE,
        negationTemplate = NegationFunctionTemplate.NOT
    )
    val blocked: Boolean,

    // ── 5. Custom function names (override templates entirely) ──────────

    /** Explicit function names: turnOn() / turnOff(). */
    @DefaultFalse(
        validFunctionName = "turnOn",
        negationFunctionName = "turnOff"
    )
    val power: Boolean,

    /** Mixed: explicit valid name + template negation. */
    @DefaultTrue(
        validFunctionName = "activate",
        negationTemplate = NegationFunctionTemplate.IS_NOT
    )
    val shield: Boolean,

    // ── 6. Additional meta-dsl 1.0.12 templates ────────────────────────

    /** ALWAYS + NEVER: alwaysRetry() / neverRetry(). */
    @DefaultFalse(
        validTemplate = ValidFunctionTemplate.ALWAYS,
        negationTemplate = NegationFunctionTemplate.NEVER
    )
    val retry: Boolean,

    /** IS_PRESENT + IS_ABSENT: isPresentFeatureX() / isAbsentFeatureX(). */
    @DefaultFalse(
        validTemplate = ValidFunctionTemplate.IS_PRESENT,
        negationTemplate = NegationFunctionTemplate.IS_ABSENT
    )
    val featureX: Boolean,

    /** PRESENT + ABSENT: presentData() / absentData(). */
    @DefaultTrue(
        validTemplate = ValidFunctionTemplate.PRESENT,
        negationTemplate = NegationFunctionTemplate.ABSENT
    )
    val data: Boolean,

    /** ENABLED + DISABLED: enabledModule() / disabledModule(). */
    @DefaultFalse(
        validTemplate = ValidFunctionTemplate.ENABLED,
        negationTemplate = NegationFunctionTemplate.DISABLED
    )
    val module: Boolean,

    /** IS_ENABLED + IS_DISABLED: isEnabledService() / isDisabledService(). */
    @DefaultTrue(
        validTemplate = ValidFunctionTemplate.IS_ENABLED,
        negationTemplate = NegationFunctionTemplate.IS_DISABLED
    )
    val service: Boolean,

    /** HAS + DOES_NOT_HAVE: hasAuth() / doesNotHaveAuth(). */
    @DefaultFalse(
        validTemplate = ValidFunctionTemplate.HAS,
        negationTemplate = NegationFunctionTemplate.DOES_NOT_HAVE
    )
    val auth: Boolean,

    /** HAS + HAS_NOT: hasLicense() / hasNotLicense(). */
    @DefaultTrue(
        validTemplate = ValidFunctionTemplate.HAS,
        negationTemplate = NegationFunctionTemplate.HAS_NOT
    )
    val license: Boolean,

    /** IS + MISSING: isVisible() / missingVisible(). */
    @DefaultTrue(
        validTemplate = ValidFunctionTemplate.IS,
        negationTemplate = NegationFunctionTemplate.MISSING
    )
    val visible: Boolean,

    /** IS + IS_MISSING: isAvailable() / isMissingAvailable(). */
    @DefaultTrue(
        validTemplate = ValidFunctionTemplate.IS,
        negationTemplate = NegationFunctionTemplate.IS_MISSING
    )
    val available: Boolean,

    /** DOES + NO: doesSync() / noSync(). */
    @DefaultFalse(
        validTemplate = ValidFunctionTemplate.DOES,
        negationTemplate = NegationFunctionTemplate.NO
    )
    val sync: Boolean
)

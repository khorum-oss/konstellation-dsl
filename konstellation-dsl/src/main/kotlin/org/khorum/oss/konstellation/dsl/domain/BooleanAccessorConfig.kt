package org.khorum.oss.konstellation.dsl.domain

/**
 * Configuration for boolean property accessor functions generated from
 * `@DefaultTrue` / `@DefaultFalse` annotations with valid/negation templates.
 *
 * When present on a [DefaultPropertyValue], the [BooleanPropSchema][org.khorum.oss.konstellation.dsl.schema.BooleanPropSchema]
 * will generate paired valid and/or negation accessor functions instead of a single setter.
 */
data class BooleanAccessorConfig(
    /** Explicit valid function name override (from annotation's `validFunctionName`). */
    val validFunctionName: String? = null,
    /** Valid template enum name: "SELF", "NONE", "IS", "WITH", "DOES", "HAS", etc. */
    val validTemplate: String? = null,
    /** Explicit negation function name override (from annotation's `negationFunctionName`). */
    val negationFunctionName: String? = null,
    /** Negation template enum name: "SELF", "NONE", "NOT", "IS_NOT", "WITHOUT", "DOES_NOT", "LACKS", etc. */
    val negationTemplate: String? = null
) {
    /** Returns true if a template value is a named pattern (not null, SELF, or NONE). */
    private fun String?.isNamedTemplate(): Boolean = this != null && this != "SELF" && this != "NONE"

    companion object {
        /** Template patterns for ValidFunctionTemplate enum entries. */
        val VALID_TEMPLATE_PATTERNS: Map<String, String> = mapOf(
            "IS" to "is{x}",
            "DOES" to "does{x}",
            "HAS" to "has{x}",
            "ENABLED" to "enabled{x}",
            "IS_ENABLED" to "isEnabled{x}",
            "WITH" to "with{x}",
            "PRESENT" to "present{x}",
            "IS_PRESENT" to "isPresent{x}",
            "ALWAYS" to "always{x}",
        )

        /** Template patterns for NegationFunctionTemplate enum entries. */
        val NEGATION_TEMPLATE_PATTERNS: Map<String, String> = mapOf(
            "DOES_NOT" to "doesNot{x}",
            "DOES_NOT_HAVE" to "doesNotHave{x}",
            "DISABLED" to "disabled{x}",
            "IS_DISABLED" to "isDisabled{x}",
            "NOT" to "not{x}",
            "IS_NOT" to "isNot{x}",
            "HAS_NOT" to "hasNot{x}",
            "LACKS" to "lacks{x}",
            "NO" to "no{x}",
            "WITHOUT" to "without{x}",
            "MISSING" to "missing{x}",
            "IS_MISSING" to "isMissing{x}",
            "ABSENT" to "absent{x}",
            "IS_ABSENT" to "isAbsent{x}",
            "NEVER" to "never{x}",
        )

        /** Paired templates: valid → negation counterpart. */
        private val VALID_TO_NEGATION_PAIR: Map<String, String> = mapOf(
            "IS" to "IS_NOT",
            "DOES" to "DOES_NOT",
            "HAS" to "LACKS",
            "ENABLED" to "DISABLED",
            "IS_ENABLED" to "IS_DISABLED",
            "WITH" to "WITHOUT",
            "PRESENT" to "ABSENT",
            "IS_PRESENT" to "IS_ABSENT",
            "ALWAYS" to "NEVER",
        )

        /** Paired templates: negation → valid counterpart. */
        private val NEGATION_TO_VALID_PAIR: Map<String, String> =
            VALID_TO_NEGATION_PAIR.entries.associate { (k, v) -> v to k }

        /**
         * Extract the semantic name `{x}` from a property name by stripping a known prefix.
         * Returns the property name with first letter capitalized if no prefix matches.
         */
        fun extractSemanticName(propName: String, templateName: String?, isNegation: Boolean): String {
            val patterns = if (isNegation) NEGATION_TEMPLATE_PATTERNS else VALID_TEMPLATE_PATTERNS
            val pattern = patterns[templateName]
            if (pattern != null) {
                val prefix = pattern.substringBefore("{x}")
                if (prefix.isNotEmpty() && propName.startsWith(prefix)) {
                    val remainder = propName.removePrefix(prefix)
                    if (remainder.isNotEmpty()) return remainder
                }
            }
            return propName.first().uppercase() + propName.substring(1)
        }

        /**
         * Maps each negation template to the valid templates whose prefixes it should strip.
         * Only semantically related prefixes are stripped to avoid incorrect name mangling
         * (e.g. NOT should not strip "is" from "isCool" → "notIsCool" is correct).
         */
        private val NEGATION_STRIPS_VALID: Map<String, List<String>> = mapOf(
            "IS_NOT" to listOf("IS"),
            "DOES_NOT" to listOf("DOES"),
            "DOES_NOT_HAVE" to listOf("HAS"),
            "LACKS" to listOf("HAS"),
            "HAS_NOT" to listOf("HAS"),
            "DISABLED" to listOf("IS_ENABLED", "ENABLED"),
            "IS_DISABLED" to listOf("IS_ENABLED", "ENABLED"),
            "WITHOUT" to listOf("WITH"),
            "ABSENT" to listOf("IS_PRESENT", "PRESENT"),
            "IS_ABSENT" to listOf("IS_PRESENT", "PRESENT"),
            "NEVER" to listOf("ALWAYS"),
        )

        /**
         * Auto-detect a semantically related valid prefix or suffix in the property name
         * and apply the negation template in the same position.
         *
         * Handles both prefix form ("hasTouch" → "doesNotHaveTouch") and
         * suffix form ("someItemEnabled" → "someItemDisabled").
         *
         * Only strips prefixes/suffixes that are semantically related to the negation
         * template (e.g. DOES_NOT_HAVE strips "has", DISABLED strips "enabled"/"isEnabled").
         * Generic negation templates like NOT and NO do not strip any prefix.
         */
        internal fun resolveNegationByAutoDetect(propName: String, negTemplate: String): String? {
            val negPattern = NEGATION_TEMPLATE_PATTERNS[negTemplate] ?: return null
            val negPrefix = negPattern.substringBefore("{x}")

            val strippableValids = NEGATION_STRIPS_VALID[negTemplate]
            if (strippableValids != null) {
                val validPrefixes = strippableValids
                    .mapNotNull { VALID_TEMPLATE_PATTERNS[it]?.substringBefore("{x}") }
                    .filter { it.isNotEmpty() }
                    .sortedByDescending { it.length }

                // 1. Try prefix match: "hasTouch" starts with "has" → "doesNotHave" + "Touch"
                for (validPrefix in validPrefixes) {
                    if (propName.startsWith(validPrefix)) {
                        val semantic = propName.removePrefix(validPrefix)
                        if (semantic.isNotEmpty()) {
                            return negPrefix + semantic
                        }
                    }
                }

                // 2. Try suffix match: "someItemEnabled" ends with "Enabled" → "someItem" + "Disabled"
                for (validPrefix in validPrefixes) {
                    val validSuffix = validPrefix.replaceFirstChar { it.uppercase() }
                    if (propName.endsWith(validSuffix) && propName.length > validSuffix.length) {
                        val root = propName.removeSuffix(validSuffix)
                        val negSuffix = negPrefix.replaceFirstChar { it.uppercase() }
                        return root + negSuffix
                    }
                }
            }

            // No related prefix/suffix found — apply as prefix with capitalized name
            return negPrefix + propName.replaceFirstChar { it.uppercase() }
        }

        /**
         * Resolve the function name from a template and semantic name.
         */
        fun applyTemplate(templateName: String, semanticName: String, isNegation: Boolean): String? {
            val patterns = if (isNegation) NEGATION_TEMPLATE_PATTERNS else VALID_TEMPLATE_PATTERNS
            val pattern = patterns[templateName] ?: return null
            return pattern.replace("{x}", semanticName)
        }

        /**
         * Get the paired counterpart template name.
         */
        fun pairedTemplate(templateName: String, isNegation: Boolean): String? {
            return if (isNegation) NEGATION_TO_VALID_PAIR[templateName] else VALID_TO_NEGATION_PAIR[templateName]
        }
    }

    /**
     * Resolve the valid function name for the given property.
     * Returns null if the valid function should not be generated.
     */
    fun resolveValidFunctionName(propName: String): String? {
        // Explicit name overrides everything
        if (!validFunctionName.isNullOrBlank()) return validFunctionName

        return when (validTemplate) {
            "SELF" -> propName
            "NONE" -> null
            null -> propName // default: use property name
            else -> {
                val semanticName = resolveSemanticName(propName)
                applyTemplate(validTemplate, semanticName, isNegation = false)
            }
        }
    }

    /**
     * Resolve the negation function name for the given property.
     * Returns null if the negation function should not be generated.
     */
    fun resolveNegationFunctionName(propName: String): String? {
        // Explicit name overrides everything
        if (!negationFunctionName.isNullOrBlank()) return negationFunctionName

        return when (negationTemplate) {
            "SELF" -> propName
            "NONE" -> null
            null -> null // default: no negation unless specified
            else -> {
                if (validTemplate.isNamedTemplate()) {
                    // Valid template is explicitly set — use standard semantic extraction
                    val semanticName = resolveSemanticName(propName)
                    applyTemplate(negationTemplate, semanticName, isNegation = true)
                } else {
                    // validTemplate is SELF, null, or NONE — use smart auto-detection
                    // which handles prefix stripping, suffix replacement, and fallback
                    resolveNegationByAutoDetect(propName, negationTemplate)
                }
            }
        }
    }

    /**
     * Determine the semantic name ({x}) from the property name.
     * Tries the SELF template first (whichever is SELF), then tries to strip known prefixes.
     */
    private fun resolveSemanticName(propName: String): String {
        // If negation is SELF, the property name IS the negation form — extract {x} from the paired valid template
        if (negationTemplate == "SELF" && validTemplate.isNamedTemplate()) {
            val paired = pairedTemplate(validTemplate!!, isNegation = false)
            return extractSemanticName(propName, paired, isNegation = true)
        }
        // If valid is SELF, the property name IS the valid form — extract {x} from the paired negation template
        if (validTemplate == "SELF" && negationTemplate.isNamedTemplate()) {
            val paired = pairedTemplate(negationTemplate!!, isNegation = true)
            return extractSemanticName(propName, paired, isNegation = false)
        }
        // At least one template must be named since resolveSemanticName is only called
        // from the else branch of resolveValid/NegationFunctionName where template is named
        return if (validTemplate.isNamedTemplate()) {
            extractSemanticName(propName, validTemplate!!, isNegation = false)
        } else {
            extractSemanticName(propName, negationTemplate!!, isNegation = true)
        }
    }
}

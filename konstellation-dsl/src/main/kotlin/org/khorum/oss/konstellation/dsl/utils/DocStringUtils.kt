package org.khorum.oss.konstellation.dsl.utils

/**
 * Cleans a raw KDoc string from [KSDeclaration.docString].
 *
 * KSP strips the outer `/** ... */` delimiters but may leave leading `*` prefixes
 * on continuation lines and surrounding whitespace. This function normalises the
 * content while preserving inline references such as `[ClassName]`, `@see`, and
 * `@param`.
 *
 * @return the cleaned string, or `null` when the input is `null` or blank after cleaning.
 */
fun cleanDocString(raw: String?): String? {
    if (raw == null) return null

    val cleaned = raw
        .lines()
        .map { line ->
            line.trimStart().removePrefix("*").let { stripped ->
                // Only trim a single leading space after the `*` prefix so that
                // intentional indentation (e.g. code samples) is kept.
                if (stripped.startsWith(" ")) stripped.substring(1) else stripped
            }.trimEnd()
        }
        .dropWhile { it.isBlank() }
        .dropLastWhile { it.isBlank() }
        .joinToString("\n")

    return cleaned.ifBlank { null }
}

package net.primal.android.notes.translation

/**
 * Protects Nostr / Lightning / web entities from machine translation.
 *
 * Some spans of a note must never be sent through a translation provider as
 * prose: Nostr identifiers and event references, nostr: URIs, Lightning
 * invoices and LNURLs, Cashu tokens, on-chain addresses, URLs, hashtags,
 * @mentions and the `#[n]` placeholders Primal uses for quoted content.
 * Providers routinely mangle them (inserting spaces, "translating" bech32,
 * dropping punctuation), which corrupts the note.
 *
 * [maskProtectedTokens] swaps every such span for an opaque placeholder
 * (`__PROTECTED_0__`, `__PROTECTED_1__`, ...) before translation and
 * [restoreProtectedTokens] swaps them back afterwards. The restore pass is
 * defensive: providers sometimes reformat the placeholders themselves
 * (splitting them with spaces or dashes), so a second-chance pattern
 * tolerates that mangling.
 *
 * Ported from the web implementation in PrimalHQ/primal-web-app
 * (src/lib/translationSanitizer.ts, PR #240), with three deliberate
 * hardening deviations: the URL pattern also stops at a closing parenthesis
 * or bracket (notes often wrap URLs in parentheses), bare bech32 entities
 * require at least 20 charset characters (avoids false positives on prose
 * words), and the mangled-placeholder restore also tolerates spaces around
 * the placeholder parts. Additionally, `nsec`/`ncryptsec` are masked as
 * well (the web pattern omits them): a note that leaks a secret key must
 * never forward it to a translation endpoint. For the same reason, bare
 * 64-character hex strings are masked too: they are note/event ids,
 * pubkeys, txids or — critically — legacy hex private keys, none of which
 * ever need to reach a translation provider.
 */
object TranslationSanitizer {

    // Order matters: nostr: URIs and bech32 entities first, then the generic
    // spans. Each alternative is a span that must survive translation verbatim.
    private val PROTECTED_TOKEN_RE = Regex(
        """\bnostr:(?:npub|nprofile|note|nevent|naddr|nrelay|nsec|ncryptsec)1[qpzry9x8gf2tvdw0s3jn54khce6mua7l]+""" +
            """|\b(?:npub|nprofile|note|nevent|naddr|nrelay|nsec|ncryptsec)1[qpzry9x8gf2tvdw0s3jn54khce6mua7l]{20,}""" +
            """|\b[0-9a-f]{64}\b""" +
            """|#\[\d+]""" +
            """|https?://[^\s<>\"')\]]+""" +
            """|www\.[^\s<>\"')\]]+""" +
            """|lightning:[^\s]+""" +
            """|\bln(?:bc|tb|bcrt|sb)[0-9a-z]+""" +
            """|\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b""" +
            """|\b(?:lno1|lni1|lnurl1)[qpzry9x8gf2tvdw0s3jn54khce6mua7l]+""" +
            """|\bcashu[AB][A-Za-z0-9+/=_-]{16,}""" +
            """|\bbc1[qpzry9x8gf2tvdw0s3jn54khce6mua7l]{20,}""" +
            """|#[\p{L}\p{N}_]+""" +
            """|@[\p{L}\p{N}_][\p{L}\p{N}_.-]*""" +
            """|:[a-zA-Z0-9_+-]+:""",
        RegexOption.IGNORE_CASE,
    )

    private const val PLACEHOLDER_PREFIX = "__PROTECTED_"
    private const val PLACEHOLDER_SUFFIX = "__"

    // Providers occasionally reformat placeholders (spaces/dashes between the
    // parts, single underscores). This tolerates the common manglings.
    private val MANGLED_PLACEHOLDER_RE = Regex(
        """_{1,2}\s*PROTECTED[\s_\-–—]*(\d+)[\s_\-–—]*_{1,2}""",
        RegexOption.IGNORE_CASE,
    )

    // The placeholder shape the restore pass will match for [prefix]: the
    // standard tolerant shape for the default prefix, or — for a salted
    // prefix — only shapes with at least the salt's leading-underscore count.
    private fun secondChanceRegex(prefix: String): Regex {
        if (prefix == PLACEHOLDER_PREFIX) return MANGLED_PLACEHOLDER_RE
        val leadingUnderscores = prefix.takeWhile { it == '_' }.length
        return Regex(
            """_{$leadingUnderscores,}\s*PROTECTED[\s_\-–—]*(\d+)[\s_\-–—]*_{1,2}""",
            RegexOption.IGNORE_CASE,
        )
    }

    private val LETTER_RE = Regex("""\p{L}""")

    /** Minimum length (after trimming) for a note to be worth translating. */
    const val MIN_TRANSLATABLE_LENGTH = 10

    /** Minimum prose length before we bother detecting a language. */
    const val MIN_PROSE_LENGTH = 4

    data class MaskedContent(
        val maskedText: String,
        val tokens: List<String>,
        /** The placeholder prefix actually used (salted if the raw content
         * already contained placeholder-like text). */
        val placeholderPrefix: String = PLACEHOLDER_PREFIX,
    )

    /** Replace every protected span in [content] with an opaque placeholder. */
    fun maskProtectedTokens(content: String): MaskedContent {
        // If the note itself already contains placeholder-like text, salt the
        // prefix (by prepending underscores) until neither the exact prefix
        // nor any placeholder shape the restore pass could match occurs in
        // the user's text; otherwise the restore pass would rewrite user text.
        var prefix = PLACEHOLDER_PREFIX
        while (content.contains(prefix) || secondChanceRegex(prefix).containsMatchIn(content)) {
            prefix = "_$prefix"
        }
        val tokens = mutableListOf<String>()
        val maskedText = PROTECTED_TOKEN_RE.replace(content) { match ->
            val index = tokens.size
            tokens.add(match.value)
            "$prefix$index$PLACEHOLDER_SUFFIX"
        }
        return MaskedContent(maskedText = maskedText, tokens = tokens, placeholderPrefix = prefix)
    }

    /** Swap placeholders back to the original spans captured by [maskProtectedTokens]. */
    fun restoreProtectedTokens(
        translated: String,
        tokens: List<String>,
        placeholderPrefix: String = PLACEHOLDER_PREFIX,
    ): String {
        if (tokens.isEmpty()) return translated
        var result = translated
        val restoredIndices = mutableSetOf<Int>()
        tokens.forEachIndexed { index, token ->
            val placeholder = "$placeholderPrefix$index$PLACEHOLDER_SUFFIX"
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, token)
                restoredIndices.add(index)
            }
        }
        // Second chance for placeholders the provider reformatted. For a
        // salted prefix only shapes carrying at least the salt's underscore
        // count are matched, so the user's own placeholder-like text (which
        // the salting loop has already out-run) is preserved.
        result = secondChanceRegex(placeholderPrefix).replace(result) { match ->
            val index = match.groupValues[1].toIntOrNull()
            if (index != null && index in tokens.indices) {
                restoredIndices.add(index)
                tokens[index]
            } else {
                match.value
            }
        }
        // If the provider dropped a placeholder entirely, its token would
        // be silently lost. Append any unrestored tokens at the end so the
        // note's links, ids and invoices survive (possibly out of place,
        // never gone).
        val missing = tokens.filterIndexed { index, _ -> index !in restoredIndices }
        if (missing.isNotEmpty()) {
            result = result.trimEnd() + " " + missing.joinToString(" ")
        }
        return result
    }

    /**
     * The translatable prose of a note: the masked text with placeholders
     * removed. Used for language detection and length checks so that a note
     * that is only a link or an npub is never offered for translation.
     */
    fun extractTranslatableProse(content: String): String {
        val masked = maskProtectedTokens(content)
        return masked.maskedText
            .replace(Regex("${Regex.escape(masked.placeholderPrefix)}\\d+__"), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    /**
     * Whether the Translate action should be offered for [content] at all.
     * Rejects notes that are too short, contain no letters, or consist solely
     * of protected tokens (links, invoices, identifiers).
     */
    fun shouldOfferTranslation(content: String): Boolean {
        val trimmed = content.trim()
        if (trimmed.length < MIN_TRANSLATABLE_LENGTH) return false
        if (!LETTER_RE.containsMatchIn(trimmed)) return false
        val prose = extractTranslatableProse(trimmed)
        return prose.length >= MIN_PROSE_LENGTH && LETTER_RE.containsMatchIn(prose)
    }
}

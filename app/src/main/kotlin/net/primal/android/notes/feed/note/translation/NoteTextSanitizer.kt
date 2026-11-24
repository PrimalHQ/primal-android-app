package net.primal.android.notes.feed.note.translation

/**
 * Protects Nostr / URL tokens so translation providers do not mangle them.
 * Mirrors the web client approach used for PrimalHQ/primal-web-app#133.
 */
object NoteTextSanitizer {
    private val tokenPatterns: List<Regex> = listOf(
        Regex("""nostr:[a-z0-9]+1[a-z0-9]{6,}""", RegexOption.IGNORE_CASE),
        Regex("""\b(npub|nprofile|note|nevent|naddr|nrelay)1[a-z0-9]{6,}\b""", RegexOption.IGNORE_CASE),
        Regex("""https?://\S+"""),
        // bolt11 / bolt12 / lnurl / lightning: URIs / cashu tokens / on-chain bech32
        Regex("""lightning:(?:lnbc|lno|lni|lnurl)[a-z0-9]+""", RegexOption.IGNORE_CASE),
        Regex("""lnbc[a-z0-9]+""", RegexOption.IGNORE_CASE),
        Regex("""lno1[a-z0-9]+""", RegexOption.IGNORE_CASE),
        Regex("""lni1[a-z0-9]+""", RegexOption.IGNORE_CASE),
        Regex("""lnurl1[a-z0-9]+""", RegexOption.IGNORE_CASE),
        Regex("""cashu[A-Za-z0-9][A-Za-z0-9+/=_-]+"""),
        Regex("""bc1[a-z0-9]+""", RegexOption.IGNORE_CASE),
        Regex("""(?<!\w)#[\w_]+"""),
        Regex("""(?<!\w)@[\w.-]+"""),
        Regex(""":[a-z0-9_+-]+:""", RegexOption.IGNORE_CASE),
    )

    data class Protected(val text: String, val tokens: List<String>)

    fun protect(input: String): Protected {
        var text = input
        val tokens = mutableListOf<String>()
        for (pattern in tokenPatterns) {
            text = pattern.replace(text) { match ->
                val idx = tokens.size
                tokens += match.value
                "⟦T$idx⟧"
            }
        }
        return Protected(text = text, tokens = tokens)
    }

    fun restore(translated: String, tokens: List<String>): String {
        var out = translated
        tokens.forEachIndexed { idx, token ->
            out = out.replace("⟦T$idx⟧", token)
            // providers sometimes drop unicode brackets
            out = out.replace("[T$idx]", token)
            out = out.replace("[[T$idx]]", token)
        }
        return out
    }
}

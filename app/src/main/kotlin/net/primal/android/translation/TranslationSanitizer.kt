package net.primal.android.translation

data class SanitizedPayload(
    val content: String,
    val placeholders: List<String>,
)

object TranslationSanitizer {
    private val protectedTokenRegex = Regex(
        pattern = """https?://[^\s<>"']+|nostr:[a-z0-9]+1[ac-hj-np-z02-9]+|lightning:[^\s<>"']+|lnbc[0-9a-z]+|lnurl1[ac-hj-np-z02-9]+|lno1[ac-hj-np-z02-9]+|cashu[A-Za-z0-9]+|bc1[ac-hj-np-z02-9]{20,}|npub1[ac-hj-np-z02-9]+|note1[ac-hj-np-z02-9]+|nevent1[ac-hj-np-z02-9]+|nprofile1[ac-hj-np-z02-9]+|naddr1[ac-hj-np-z02-9]+|nrelay1[ac-hj-np-z02-9]+|#[\p{L}\p{N}_-]+|@[\p{L}\p{N}_.\-]+""",
        option = RegexOption.IGNORE_CASE,
    )
    private val placeholderRegex = Regex("""__PRIMAL_TX_(\d+)__""")
    private val mangledPlaceholderRegex = Regex(
        """[_*]{0,2}PRIMAL[_*\s\-]?TX[_*\s\-]?(\d+)[_*]{0,2}""",
        RegexOption.IGNORE_CASE,
    )

    fun sanitizeForTranslation(raw: String): SanitizedPayload {
        val placeholders = mutableListOf<String>()
        val content = protectedTokenRegex.replace(raw) { match ->
            val index = placeholders.size
            placeholders.add(match.value)
            "__PRIMAL_TX_${index}__"
        }
        return SanitizedPayload(content = content, placeholders = placeholders)
    }

    fun restoreTranslationContent(translated: String, placeholders: List<String>): String {
        var out = placeholderRegex.replace(translated) { match ->
            val i = match.groupValues[1].toIntOrNull()
            if (i != null && i in placeholders.indices) placeholders[i] else match.value
        }
        out = mangledPlaceholderRegex.replace(out) { match ->
            val i = match.groupValues[1].toIntOrNull()
            if (i != null && i in placeholders.indices) placeholders[i] else match.value
        }
        return out
    }

    fun shouldOfferTranslation(content: String): Boolean {
        val trimmed = content.trim()
        if (trimmed.length < 12) return false
        val letters = trimmed.replace(Regex("""[^\p{L}]"""), "")
        return letters.length >= 8
    }
}

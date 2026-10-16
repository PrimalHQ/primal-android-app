package net.primal.android.notes.translation

data class ProtectedNoteText(
    val original: String,
    val masked: String,
    val tokens: List<String>,
) {
    fun restore(translated: String): String {
        if (tokens.isEmpty()) return translated
        var result = translated
        for (index in tokens.indices.reversed()) {
            result = restoreToken(text = result, index = index, token = tokens[index])
        }
        return result
    }

    fun letterCount(): Int = original.count { it.isLetter() }

    fun isTokenOnly(): Boolean {
        val remainder = tokens.fold(original) { acc, token -> acc.replace(token, " ") }
        return remainder.none { it.isLetter() }
    }
}

object NoteTokenProtector {

    private const val MIN_OFFER_LETTERS = 8

    private val tokenPatterns: List<Regex> = listOf(
        Regex("""https?://[^\s<>"'`)\]>]+""", RegexOption.IGNORE_CASE),
        Regex("""www\.[^\s<>"'`)\]>]+""", RegexOption.IGNORE_CASE),
        Regex("""nostr:[a-z0-9]+1[a-z0-9]{6,}""", RegexOption.IGNORE_CASE),
        Regex(
            """\b(npub|nprofile|note|nevent|naddr|nrelay|nsec|nsite)1[ac-hj-np-z02-9]{6,}\b""",
            RegexOption.IGNORE_CASE,
        ),
        Regex("""lightning:[^\s<>"'`)\]>]+""", RegexOption.IGNORE_CASE),
        Regex("""lnurlp://[^\s<>"'`)\]>]+""", RegexOption.IGNORE_CASE),
        Regex("""\b(lnbc|lntb|lnbcrt|lnbs)[0-9a-z]+""", RegexOption.IGNORE_CASE),
        Regex("""\b(lno1|lni1|lnurl1)[a-z0-9]+""", RegexOption.IGNORE_CASE),
        Regex("""\bcashu[AB][A-Za-z0-9+/=_-]+"""),
        Regex("""\b(bc1|tb1|bcrt1)[a-z0-9]{20,}\b""", RegexOption.IGNORE_CASE),
        Regex("""\b[13][a-km-zA-HJ-NP-Z1-9]{25,34}\b"""),
        Regex("""\b[0-9a-f]{64}\b""", RegexOption.IGNORE_CASE),
        Regex("""(?<!\w)@[A-Za-z0-9_.]+"""),
        Regex("""(?<!\w)#[A-Za-z0-9_]+"""),
        Regex(""":[a-z0-9_+-]+:""", RegexOption.IGNORE_CASE),
    )

    fun protect(text: String): ProtectedNoteText {
        val ranges = collectTokenRanges(text)
        if (ranges.isEmpty()) {
            return ProtectedNoteText(original = text, masked = text, tokens = emptyList())
        }

        val tokens = ArrayList<String>(ranges.size)
        val masked = StringBuilder(text.length)
        var cursor = 0
        ranges.forEach { range ->
            masked.append(text, cursor, range.first)
            tokens += text.substring(range.first, range.last + 1)
            masked.append(placeholder(tokens.lastIndex))
            cursor = range.last + 1
        }
        if (cursor < text.length) {
            masked.append(text, cursor, text.length)
        }
        return ProtectedNoteText(original = text, masked = masked.toString(), tokens = tokens)
    }

    fun shouldOfferTranslation(text: String): Boolean {
        val protected = protect(text)
        if (protected.isTokenOnly()) return false
        return protected.letterCount() >= MIN_OFFER_LETTERS
    }

    private fun collectTokenRanges(text: String): List<IntRange> {
        val candidates = tokenPatterns.flatMap { pattern ->
            pattern.findAll(text).map { match ->
                val adjusted = trimTrailingPunctuation(match.range, text)
                adjusted
            }
        }.sortedWith(compareBy<IntRange> { it.first }.thenByDescending { it.last - it.first })

        val accepted = ArrayList<IntRange>()
        candidates.forEach { candidate ->
            val overlaps = accepted.any { existing -> rangesOverlap(existing, candidate) }
            if (!overlaps && candidate.first <= candidate.last) {
                accepted += candidate
            }
        }
        return accepted.sortedBy { it.first }
    }

    private fun trimTrailingPunctuation(range: IntRange, text: String): IntRange {
        var end = range.last
        while (end >= range.first && text[end] in TRAILING_PUNCTUATION) {
            end--
        }
        return range.first..end
    }

    private fun rangesOverlap(left: IntRange, right: IntRange): Boolean =
        left.first <= right.last && right.first <= left.last

    internal fun placeholder(index: Int): String = "⟦NT$index⟧"
}

private val TRAILING_PUNCTUATION = charArrayOf('.', ',', ';', ':', '!', '?', ')', ']', '}', '"', '\'')

private fun restoreToken(text: String, index: Int, token: String): String {
    val patterns = listOf(
        Regex("""⟦\s*NT$index\s*⟧""", RegexOption.IGNORE_CASE),
        Regex("""\[\[\s*NT$index\s*]]""", RegexOption.IGNORE_CASE),
        Regex("""\[\s*NT$index\s*]""", RegexOption.IGNORE_CASE),
        Regex("""⟨\s*NT$index\s*⟩""", RegexOption.IGNORE_CASE),
        Regex("""<\s*NT$index\s*>""", RegexOption.IGNORE_CASE),
        Regex("""\{\{\s*NT$index\s*}}""", RegexOption.IGNORE_CASE),
        Regex("""__\s*NT$index\s*__""", RegexOption.IGNORE_CASE),
    )
    patterns.forEach { pattern ->
        if (pattern.containsMatchIn(text)) {
            return text.replace(pattern, token)
        }
    }
    val loose = Regex("""(?<![A-Za-z])NT$index(?![A-Za-z0-9])""", RegexOption.IGNORE_CASE)
    return if (loose.containsMatchIn(text)) text.replace(loose, token) else text
}

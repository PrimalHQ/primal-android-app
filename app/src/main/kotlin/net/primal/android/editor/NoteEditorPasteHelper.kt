package net.primal.android.editor

import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.utils.parseNostrUris

private const val TRAILING_PUNCTUATION = ".,;:)]}>\"'!?"

data class ExtractedNostrToken(
    val sourceMatch: String,
    val canonicalUri: String,
)

/**
 * Walk the receiver text token-by-token (whitespace-bounded) and, for each token that
 * contains a decodable NIP-19 identifier passing [isEmbeddable], return what to remove
 * from the source text plus the canonical bech32 to embed.
 *
 * Trailing punctuation in [TRAILING_PUNCTUATION] is peeled off the token before
 * computing [ExtractedNostrToken.sourceMatch], so a paste like
 * "Look at https://primal.net/e/nevent1abc." keeps the trailing period in the typed
 * text after the URL is stripped.
 */
fun String.extractEmbeddableNostrTokens(isEmbeddable: (canonicalUri: String) -> Boolean): List<ExtractedNostrToken> {
    if (isEmpty()) return emptyList()

    val tokens = split(Regex("\\s+")).filter { it.isNotEmpty() }
    return tokens.mapNotNull { token ->
        val trimmed = token.trimEndChars(TRAILING_PUNCTUATION)
        val canonical = trimmed.parseNostrUris().firstOrNull(isEmbeddable) ?: return@mapNotNull null
        ExtractedNostrToken(sourceMatch = trimmed, canonicalUri = canonical)
    }
}

private fun String.trimEndChars(chars: String): String {
    var end = length
    while (end > 0 && chars.indexOf(this[end - 1]) >= 0) end--
    return substring(0, end)
}

fun Nevent.toEmbeddableReferencedUriOrNull(uri: String): NoteEditorContract.ReferencedUri<*>? =
    when (kind) {
        NostrEventKind.Highlight.value ->
            NoteEditorContract.ReferencedUri.Highlight(
                data = null,
                loading = true,
                uri = uri,
                nevent = this,
            )

        NostrEventKind.ShortTextNote.value, null ->
            NoteEditorContract.ReferencedUri.Note(
                data = null,
                loading = true,
                uri = uri,
                nevent = this,
            )

        else -> null
    }

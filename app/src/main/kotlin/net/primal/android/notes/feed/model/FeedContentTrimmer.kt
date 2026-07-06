package net.primal.android.notes.feed.model

import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.notes.feed.note.ui.ELLIPSIZE_THRESHOLD
import net.primal.domain.nostr.utils.LnInvoiceUtils

/**
 * Correctness cushion added on top of [ELLIPSIZE_THRESHOLD] when deciding how much raw
 * content to keep for the feed preview. Not a business threshold: it covers the few minor
 * length reducers the span accounting does not model (blank-line collapsing, primal.net
 * link stripping, trim), guaranteeing the trimmed content still refines to more than
 * [ELLIPSIZE_THRESHOLD] characters so the render-time ellipsize behaves identically.
 */
internal const val FEED_TRIM_SAFETY_MARGIN = 40

internal const val FEED_CONTENT_TRIM_BUDGET = ELLIPSIZE_THRESHOLD + FEED_TRIM_SAFETY_MARGIN

/**
 * Produces a trimmed-for-feed prefix of [content] so the feed render pipeline only ever
 * parses a bounded amount of text. Characters that the render refinement strips (media/link
 * URLs, nostr URIs, the lightning invoice) do not count toward the budget, so URL-heavy
 * prefixes are kept in full. Returns [content] unchanged when it is short enough that no
 * trimming is needed.
 */
internal fun computeFeedContent(
    content: String,
    uris: List<EventUriUi>,
    nostrUris: List<NoteNostrUriUi>,
): String {
    if (content.length <= FEED_CONTENT_TRIM_BUDGET) return content

    val removalTexts = buildList {
        uris.forEach { add(it.url) }
        nostrUris.forEach { add(it.uri) }
        LnInvoiceUtils.findInvoice(content)?.let { add(it) }
    }.filter { it.isNotEmpty() }

    val removed = BooleanArray(content.length)
    for (text in removalTexts) {
        var index = content.indexOf(text)
        while (index >= 0) {
            for (i in index until index + text.length) removed[i] = true
            index = content.indexOf(text, startIndex = index + text.length)
        }
    }

    var surviving = 0
    var cutIndex = content.length
    for (i in content.indices) {
        if (!removed[i]) surviving++
        if (surviving > FEED_CONTENT_TRIM_BUDGET) {
            cutIndex = i + 1
            break
        }
    }

    return content.substring(0, cutIndex)
}

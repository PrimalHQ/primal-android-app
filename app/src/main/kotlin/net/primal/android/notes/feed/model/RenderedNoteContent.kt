package net.primal.android.notes.feed.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import net.primal.android.core.compose.attachment.model.isMediaUri
import net.primal.android.core.utils.TextMatcher
import net.primal.domain.links.EventUriNostrType
import net.primal.domain.links.EventUriType
import net.primal.domain.nostr.utils.clearAtSignFromNostrUris

internal const val PROFILE_ID_ANNOTATION_TAG = "profileId"
internal const val URL_ANNOTATION_TAG = "url"
internal const val NOTE_ANNOTATION_TAG = "note"
internal const val HASHTAG_ANNOTATION_TAG = "hashtag"
internal const val NOSTR_ADDRESS_ANNOTATION_TAG = "naddr"

internal const val ELLIPSIZE_THRESHOLD = 300

private const val ELLIPSIZE_URL_THRESHOLD = 40
private const val ELLIPSIZE_URL_TEXT = "..."

@Immutable
data class RenderedNoteContent(
    val refinedText: String,
    val shouldEllipsize: Boolean,
    val annotations: List<ContentAnnotation>,
)

@Immutable
data class ContentAnnotation(
    val tag: String,
    val item: String,
    val start: Int,
    val end: Int,
)

fun computeRenderedNoteContent(
    data: NoteContentUi,
    expanded: Boolean,
    shouldKeepNostrNoteUris: Boolean = false,
): RenderedNoteContent {
    val mediaAttachments = data.uris.filter { it.isMediaUri() }
    val linkAttachments = data.uris.filterNot { it.isMediaUri() }

    val effectiveNostrUris = data.nostrUris.filterNot { nostrUri ->
        data.uris.any { uri ->
            uri.url.contains(nostrUri.uri) &&
                data.content.indexOf(uri.url).takeIf { it >= 0 }?.let { uriIndex ->
                    val nostrIndex = data.content.indexOf(nostrUri.uri, startIndex = uriIndex)
                    nostrIndex in uriIndex until (uriIndex + uri.url.length)
                } == true
        }
    }

    val mentionedUsers = effectiveNostrUris.filter(type = EventUriNostrType.Profile)
    val unhandledNostrAddressUris = effectiveNostrUris.filterUnhandledNostrAddressUris()

    val refinedContent = data.content
        .clearAtSignFromNostrUris()
        .remove(texts = mediaAttachments.map { it.url })
        .remove(texts = linkAttachments.filter { it.title?.isNotEmpty() == true }.map { it.url })
        .remove(texts = linkAttachments.filter { it.type == EventUriType.Audio }.map { it.url })
        .replaceNostrProfileUrisWithHandles(resources = mentionedUsers)
        .remove(texts = if (!shouldKeepNostrNoteUris) effectiveNostrUris.map { it.uri } else emptyList())
        .remove(texts = data.invoices)
        .clearParsedPrimalLinks()
        .limitLineBreaks(maxBreaks = 2)
        .trim()
        .ellipsizeLongUrls(texts = linkAttachments.filter { it.title?.isNotEmpty() != true }.map { it.url })

    val shouldEllipsize = !expanded && refinedContent.length > ELLIPSIZE_THRESHOLD
    val truncatedContent = if (shouldEllipsize) refinedContent.substring(0, ELLIPSIZE_THRESHOLD) else refinedContent

    return RenderedNoteContent(
        refinedText = truncatedContent,
        shouldEllipsize = shouldEllipsize,
        annotations = buildContentAnnotations(
            content = truncatedContent,
            data = data,
            unhandledNostrAddressUris = unhandledNostrAddressUris,
            mentionedUsers = mentionedUsers,
        ),
    )
}

private fun buildContentAnnotations(
    content: String,
    data: NoteContentUi,
    unhandledNostrAddressUris: List<NoteNostrUriUi>,
    mentionedUsers: List<NoteNostrUriUi>,
): List<ContentAnnotation> =
    buildList {
        unhandledNostrAddressUris.forEach { nostrUri ->
            val startIndex = content.indexOf(nostrUri.uri)
            if (startIndex >= 0) {
                add(
                    ContentAnnotation(
                        tag = NOSTR_ADDRESS_ANNOTATION_TAG,
                        item = nostrUri.uri,
                        start = startIndex,
                        end = startIndex + nostrUri.uri.length,
                    ),
                )
            }
        }

        data.uris
            .filterNot { it.isMediaUri() }
            .filterNot { it.type == EventUriType.Audio }
            .map { it.url }
            .forEach { url ->
                val shownUrl = if (url.length > ELLIPSIZE_URL_THRESHOLD) {
                    url.take(ELLIPSIZE_URL_THRESHOLD) + ELLIPSIZE_URL_TEXT
                } else {
                    url
                }
                var startIndex = content.indexOf(shownUrl)
                while (startIndex >= 0) {
                    add(
                        ContentAnnotation(
                            tag = URL_ANNOTATION_TAG,
                            item = url,
                            start = startIndex,
                            end = startIndex + shownUrl.length,
                        ),
                    )
                    startIndex = content.indexOf(shownUrl, startIndex + 1)
                }
            }

        mentionedUsers.forEach { mention ->
            checkNotNull(mention.referencedUser)
            val displayHandle = mention.referencedUser.displayUsername
            var startIndex = content.indexOf(displayHandle)
            while (startIndex >= 0) {
                add(
                    ContentAnnotation(
                        tag = PROFILE_ID_ANNOTATION_TAG,
                        item = mention.referencedUser.userId,
                        start = startIndex,
                        end = startIndex + displayHandle.length,
                    ),
                )
                startIndex = content.indexOf(displayHandle, startIndex + 1)
            }
        }

        TextMatcher(content = content, texts = data.hashtags, repeatingOccurrences = true)
            .matches()
            .forEach { textMatch ->
                add(
                    ContentAnnotation(
                        tag = HASHTAG_ANNOTATION_TAG,
                        item = textMatch.value,
                        start = textMatch.startIndex,
                        end = textMatch.endIndex,
                    ),
                )
            }
    }

fun RenderedNoteContent.toAnnotatedString(seeMoreText: String, highlightColor: Color): AnnotatedString {
    val fullText = if (shouldEllipsize) "$refinedText $seeMoreText" else refinedText
    return buildAnnotatedString {
        append(fullText)

        if (fullText.endsWith(seeMoreText)) {
            addStyle(
                style = SpanStyle(color = highlightColor),
                start = fullText.length - seeMoreText.length,
                end = fullText.length,
            )
        }

        annotations.forEach {
            addStyle(
                style = SpanStyle(color = highlightColor),
                start = it.start,
                end = it.end,
            )
            addStringAnnotation(
                tag = it.tag,
                annotation = it.item,
                start = it.start,
                end = it.end,
            )
        }
    }
}

private fun List<NoteNostrUriUi>.filter(type: EventUriNostrType) = filter { it.type == type }

private fun List<NoteNostrUriUi>.filterUnhandledNostrAddressUris() =
    filter {
        it.uri.contains("naddr") && it.referencedUser == null && it.referencedNote == null
    }

private fun String.remove(texts: List<String>): String {
    var newContent = this
    texts.forEach {
        newContent = newContent.replace(it, "")
    }
    return newContent
}

private fun String.ellipsizeLongUrls(texts: List<String>): String {
    var newContent = this
    texts.filter { it.length > ELLIPSIZE_URL_THRESHOLD }.forEach {
        newContent = newContent.replace(it, it.take(ELLIPSIZE_URL_THRESHOLD) + ELLIPSIZE_URL_TEXT)
    }
    return newContent
}

private fun String.replaceNostrProfileUrisWithHandles(resources: List<NoteNostrUriUi>): String {
    var newContent = this
    resources.forEach {
        checkNotNull(it.referencedUser)
        newContent = newContent.replace(
            oldValue = it.uri,
            newValue = it.referencedUser.displayUsername,
            ignoreCase = true,
        )
    }
    return newContent
}

private val noteLinkLeftovers = listOf(
    "https://primal.net/e/ " to "",
    "https://www.primal.net/e/ " to "",
    "http://primal.net/e/ " to "",
    "http://www.primal.net/e/ " to "",
    "https://primal.net/e/\n" to "",
    "https://www.primal.net/e/\n" to "",
    "http://primal.net/e/\n" to "",
    "http://www.primal.net/e/\n" to "",
)

private val profileLinkLeftovers = listOf(
    "https://primal.net/p/@" to "@",
    "https://www.primal.net/p/@" to "@",
    "http://primal.net/p/@" to "@",
    "http://www.primal.net/p/@" to "@",
)

private fun String.clearParsedPrimalLinks(): String {
    var newContent = this
    (noteLinkLeftovers + profileLinkLeftovers).forEach {
        newContent = newContent.replace(
            oldValue = it.first,
            newValue = it.second,
            ignoreCase = false,
        )
    }
    noteLinkLeftovers.map { it.first.trim() }.toSet().forEach {
        if (newContent.endsWith(it)) {
            newContent = newContent.replace(
                oldValue = it,
                newValue = "",
            )
        }
    }
    return newContent
}

private fun String.limitLineBreaks(maxBreaks: Int = 2): String {
    val maxBreakPattern = "(\\n){${maxBreaks + 1},}".toRegex()
    return this.replace(maxBreakPattern, "\n".repeat(maxBreaks))
}

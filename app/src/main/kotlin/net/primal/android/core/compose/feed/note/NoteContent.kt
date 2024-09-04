package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.R
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.attachment.model.isMediaAttachment
import net.primal.android.core.compose.feed.model.NoteContentUi
import net.primal.android.core.compose.feed.model.NoteNostrUriUi
import net.primal.android.core.compose.feed.note.events.InvoicePayClickEvent
import net.primal.android.core.compose.feed.note.events.MediaClickEvent
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.utils.HashtagMatch
import net.primal.android.core.utils.HashtagMatcher
import net.primal.android.nostr.ext.cleanNostrUris
import net.primal.android.notes.db.ReferencedPost
import net.primal.android.notes.db.ReferencedUser
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

private const val PROFILE_ID_ANNOTATION_TAG = "profileId"
private const val URL_ANNOTATION_TAG = "url"
private const val NOTE_ANNOTATION_TAG = "note"
private const val HASHTAG_ANNOTATION_TAG = "hashtag"
private const val NOSTR_ADDRESS_ANNOTATION_TAG = "naddr"

private fun List<NoteNostrUriUi>.filterMentionedPosts() = filter { it.referencedPost != null }

private fun List<NoteNostrUriUi>.filterMentionedUsers() = filter { it.referencedUser != null }

private fun List<NoteNostrUriUi>.filterUnhandledNostrAddressUris() =
    filter {
        it.uri.contains("naddr") && it.referencedUser == null && it.referencedPost == null
    }

private fun String.remove(texts: List<String>): String {
    var newContent = this
    texts.forEach {
        newContent = newContent.replace(it, "")
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

private const val ELLIPSIZE_THRESHOLD = 500

private fun String.ellipsize(expanded: Boolean, ellipsizeText: String): String {
    val shouldEllipsize = length > ELLIPSIZE_THRESHOLD
    return if (expanded || !shouldEllipsize) {
        this
    } else {
        substring(0, ELLIPSIZE_THRESHOLD).plus(" $ellipsizeText")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteContent(
    modifier: Modifier = Modifier,
    data: NoteContentUi,
    expanded: Boolean,
    textSelectable: Boolean = false,
    highlightColor: Color = AppTheme.colorScheme.secondary,
    contentColor: Color = AppTheme.colorScheme.onSurface,
    referencedNoteContainerColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
    onProfileClick: ((String) -> Unit)? = null,
    onPostClick: ((String) -> Unit)? = null,
    onArticleClick: ((naddr: String) -> Unit)? = null,
    onClick: ((Offset) -> Unit)? = null,
    onUrlClick: ((String) -> Unit)? = null,
    onHashtagClick: ((String) -> Unit)? = null,
    onMediaClick: ((MediaClickEvent) -> Unit)? = null,
    onPayInvoiceClick: ((InvoicePayClickEvent) -> Unit)? = null,
) {
    val displaySettings = LocalContentDisplaySettings.current
    val seeMoreText = stringResource(id = R.string.feed_see_more)
    val contentText = remember {
        renderContentAsAnnotatedString(
            data = data,
            expanded = expanded,
            seeMoreText = seeMoreText,
            highlightColor = highlightColor,
        )
    }

    Column(modifier = modifier) {
        if (contentText.isNotEmpty()) {
            PrimalClickableText(
                style = AppTheme.typography.bodyMedium.copy(
                    color = contentColor,
                    fontSize = displaySettings.contentAppearance.noteBodyFontSize,
                    lineHeight = displaySettings.contentAppearance.noteBodyLineHeight,
                ),
                text = contentText,
                textSelectable = textSelectable,
                onClick = { position, offset ->
                    val annotation = contentText.getStringAnnotations(
                        start = position,
                        end = position,
                    ).firstOrNull()

                    annotation?.handleAnnotationClick(
                        onProfileClick = onProfileClick,
                        onUrlClick = onUrlClick,
                        onPostClick = onPostClick,
                        onHashtagClick = onHashtagClick,
                        onArticleClick = onArticleClick,
                    ) ?: onClick?.invoke(offset)
                },
            )
        }

        if (data.invoices.isNotEmpty()) {
            NoteLightningInvoice(
                modifier = Modifier.padding(top = if (contentText.isEmpty()) 4.dp else 6.dp),
                invoice = data.invoices.first(),
                onPayClick = { lnbc -> onPayInvoiceClick?.invoke(InvoicePayClickEvent(lnbc = lnbc)) },
            )
        }

        if (data.attachments.isNotEmpty()) {
            NoteAttachments(
                modifier = Modifier.padding(top = if (contentText.isEmpty()) 4.dp else 6.dp),
                attachments = data.attachments,
                onUrlClick = onUrlClick,
                onMediaClick = onMediaClick,
            )
        }

        val referencedPostResources = data.nostrUris.filterMentionedPosts()
        if (referencedPostResources.isNotEmpty()) {
            ReferencedNotesColumn(
                modifier = Modifier.padding(top = 4.dp),
                postResources = referencedPostResources,
                expanded = expanded,
                containerColor = referencedNoteContainerColor,
                onPostClick = onPostClick,
                onArticleClick = onArticleClick,
                onMediaClick = onMediaClick,
                onPayInvoiceClick = onPayInvoiceClick,
            )
        }
    }
}

private fun AnnotatedString.Range<String>.handleAnnotationClick(
    onProfileClick: ((String) -> Unit)?,
    onUrlClick: ((String) -> Unit)?,
    onPostClick: ((String) -> Unit)?,
    onHashtagClick: ((String) -> Unit)?,
    onArticleClick: ((naddr: String) -> Unit)?,
) = when (this.tag) {
    PROFILE_ID_ANNOTATION_TAG -> onProfileClick?.invoke(this.item)
    URL_ANNOTATION_TAG -> onUrlClick?.invoke(this.item)
    NOTE_ANNOTATION_TAG -> onPostClick?.invoke(this.item)
    HASHTAG_ANNOTATION_TAG -> onHashtagClick?.invoke(this.item)
    NOSTR_ADDRESS_ANNOTATION_TAG -> {
        this.item.split(":").lastOrNull()?.let { address ->
            onArticleClick?.invoke(address)
        }
    }

    else -> Unit
}

fun renderContentAsAnnotatedString(
    data: NoteContentUi,
    expanded: Boolean,
    seeMoreText: String,
    highlightColor: Color,
    shouldKeepNostrNoteUris: Boolean = false,
): AnnotatedString {
    val mediaAttachments = data.attachments.filter { it.isMediaAttachment() }
    val linkAttachments = data.attachments.filterNot { it.isMediaAttachment() }
    val mentionedPosts = data.nostrUris.filterMentionedPosts()
    val mentionedUsers = data.nostrUris.filterMentionedUsers()
    val unhandledNostrAddressUris = data.nostrUris.filterUnhandledNostrAddressUris()

    val shouldDeleteLinks = mediaAttachments.isEmpty() && linkAttachments.size == 1 &&
        linkAttachments.first().let { singleLink ->
            data.content.trim().endsWith(singleLink.url) &&
                (!singleLink.title.isNullOrBlank() || !singleLink.description.isNullOrBlank())
        }

    val refinedContent = data.content
        .cleanNostrUris()
        .remove(texts = mediaAttachments.map { it.url })
        .remove(texts = if (!shouldKeepNostrNoteUris) mentionedPosts.map { it.uri } else emptyList())
        .remove(texts = if (shouldDeleteLinks) linkAttachments.map { it.url } else emptyList())
        .remove(texts = data.invoices)
        .replaceNostrProfileUrisWithHandles(resources = mentionedUsers)
        .clearParsedPrimalLinks()
        .trim()
        .ellipsize(expanded = expanded, ellipsizeText = seeMoreText)

    return buildAnnotatedString {
        append(refinedContent)

        if (refinedContent.endsWith(seeMoreText)) {
            addStyle(
                style = SpanStyle(color = highlightColor),
                start = refinedContent.length - seeMoreText.length,
                end = refinedContent.length,
            )
        }

        unhandledNostrAddressUris.forEach {
            addNostrAddressAnnotation(
                nostrUri = it,
                content = refinedContent,
                highlightColor = highlightColor,
            )
        }

        data.attachments
            .filterNot { it.isMediaAttachment() }
            .map { it.url }
            .forEach {
                addUrlAnnotation(
                    url = it,
                    content = refinedContent,
                    highlightColor = highlightColor,
                )
            }

        mentionedUsers.forEach {
            checkNotNull(it.referencedUser)
            addProfileAnnotation(
                referencedUser = it.referencedUser,
                content = refinedContent,
                highlightColor = highlightColor,
            )
        }

        HashtagMatcher(content = refinedContent, hashtags = data.hashtags)
            .matches()
            .forEach {
                addHashtagAnnotation(
                    hashtagMatch = it,
                    highlightColor = highlightColor,
                )
            }
    }
}

private fun AnnotatedString.Builder.addHashtagAnnotation(highlightColor: Color, hashtagMatch: HashtagMatch) {
    addStyle(
        style = SpanStyle(color = highlightColor),
        start = hashtagMatch.startIndex,
        end = hashtagMatch.endIndex,
    )
    addStringAnnotation(
        tag = HASHTAG_ANNOTATION_TAG,
        annotation = hashtagMatch.value,
        start = hashtagMatch.startIndex,
        end = hashtagMatch.endIndex,
    )
}

private fun AnnotatedString.Builder.addProfileAnnotation(
    referencedUser: ReferencedUser,
    content: String,
    highlightColor: Color,
) {
    val displayHandle = referencedUser.displayUsername
    val startIndex = content.indexOf(displayHandle)
    if (startIndex >= 0) {
        val endIndex = startIndex + displayHandle.length
        addStyle(
            style = SpanStyle(color = highlightColor),
            start = startIndex,
            end = endIndex,
        )
        addStringAnnotation(
            tag = PROFILE_ID_ANNOTATION_TAG,
            annotation = referencedUser.userId,
            start = startIndex,
            end = endIndex,
        )
    }
}

private fun AnnotatedString.Builder.addUrlAnnotation(
    url: String,
    content: String,
    highlightColor: Color,
) {
    val startIndex = content.indexOf(url)
    if (startIndex >= 0) {
        val endIndex = startIndex + url.length
        addStyle(
            style = SpanStyle(color = highlightColor),
            start = startIndex,
            end = endIndex,
        )
        addStringAnnotation(
            tag = URL_ANNOTATION_TAG,
            annotation = url,
            start = startIndex,
            end = endIndex,
        )
    }
}

private fun AnnotatedString.Builder.addNostrAddressAnnotation(
    nostrUri: NoteNostrUriUi,
    content: String,
    highlightColor: Color,
) {
    val startIndex = content.indexOf(nostrUri.uri)
    if (startIndex >= 0) {
        val endIndex = startIndex + nostrUri.uri.length
        addStyle(
            style = SpanStyle(color = highlightColor),
            start = startIndex,
            end = endIndex,
        )
        addStringAnnotation(
            tag = NOSTR_ADDRESS_ANNOTATION_TAG,
            annotation = nostrUri.uri,
            start = startIndex,
            end = endIndex,
        )
    }
}

@Preview
@Composable
fun PreviewPostContent() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        Surface {
            NoteContent(
                data = NoteContentUi(
                    noteId = "",
                    content = """
                        Unfortunately the days of using pseudonyms in metaspace are numbered. #nostr 
                        nostr:referencedUser
                    """.trimIndent(),
                    attachments = emptyList(),
                    nostrUris = listOf(
                        NoteNostrUriUi(
                            uri = "nostr:referencedUser",
                            referencedPost = null,
                            referencedUser = ReferencedUser(
                                userId = "nostr:referencedUser",
                                handle = "alex",
                            ),
                        ),
                    ),
                    hashtags = listOf("#nostr"),
                ),
                expanded = false,
                onProfileClick = {},
                onPostClick = {},
                onArticleClick = {},
                onClick = {},
                onUrlClick = {},
                onHashtagClick = {},
                onMediaClick = {},
                onPayInvoiceClick = {},
            )
        }
    }
}

@Preview
@Composable
fun PreviewPostContentWithReferencedPost() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        Surface {
            NoteContent(
                data = NoteContentUi(
                    noteId = "",
                    content = """
                        Unfortunately the days of using pseudonyms in metaspace are numbered. #nostr
                        
                        nostr:referencedPost
                        
                        Or maybe not.
                        
                        nostr:referenced2Post
                    """.trimIndent(),
                    attachments = emptyList(),
                    nostrUris = listOf(
                        NoteNostrUriUi(
                            uri = "nostr:referencedPost",
                            referencedPost = ReferencedPost(
                                postId = "postId",
                                createdAt = 0,
                                content = "This is referenced post.",
                                authorId = "authorId",
                                authorName = "primal",
                                authorAvatarCdnImage = null,
                                authorInternetIdentifier = "hi@primal.net",
                                authorLightningAddress = "h@getalby.com",
                                attachments = emptyList(),
                                nostrUris = emptyList(),
                            ),
                            referencedUser = null,
                        ),
                        NoteNostrUriUi(
                            uri = "nostr:referenced2Post",
                            referencedPost = ReferencedPost(
                                postId = "postId",
                                createdAt = 0,
                                content = "This is referenced post #2.",
                                authorId = "authorId",
                                authorName = "primal",
                                authorAvatarCdnImage = null,
                                authorInternetIdentifier = "hi@primal.net",
                                authorLightningAddress = "h@getalby.com",
                                attachments = emptyList(),
                                nostrUris = emptyList(),
                            ),
                            referencedUser = null,
                        ),
                    ),
                    hashtags = listOf("#nostr"),
                    invoices = listOf(
                        "lnbc888550n1pnp6fz9pp5als09l5nfj9pkqk7mpj6cz6075nd4v95ljz0p65n8zkz03p75t3" +
                            "sdp9wdshgueqvehhygr3v9q8qunfd4skctnwv46r5cqzzsxqrrs0fppqyyu34ypjxgclynk64hz2r6" +
                            "ddudpaf5mesp5c8mv8xdu67pra93m3j9aw9mxh08gk09upmjsdpspjxcgcrfjyc0s9qyyssqng6uu0" +
                            "z84h7wlcrlyqywl6jlfd4630k4yd056d3q9h9rg9tzmza5adpzjn489fees4vq0armdskuqgxxvug3" +
                            "et34cqdxj6ldu8lkd2cqcvx5am",
                    ),
                ),
                expanded = false,
                onProfileClick = {},
                onPostClick = {},
                onArticleClick = {},
                onClick = {},
                onUrlClick = {},
                onHashtagClick = {},
                onMediaClick = {},
                onPayInvoiceClick = {},
            )
        }
    }
}

package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Instant
import net.primal.android.R
import net.primal.android.core.activity.LocalContentDisplaySettings
import net.primal.android.core.activity.LocalPrimalTheme
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Document
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.zaps.ReferencedNoteZap
import net.primal.android.core.compose.zaps.ReferencedZap
import net.primal.android.notes.feed.model.HASHTAG_ANNOTATION_TAG
import net.primal.android.notes.feed.model.NOSTR_ADDRESS_ANNOTATION_TAG
import net.primal.android.notes.feed.model.NOTE_ANNOTATION_TAG
import net.primal.android.notes.feed.model.NoteContentUi
import net.primal.android.notes.feed.model.NoteNostrUriUi
import net.primal.android.notes.feed.model.PROFILE_ID_ANNOTATION_TAG
import net.primal.android.notes.feed.model.RenderedNoteContent
import net.primal.android.notes.feed.model.URL_ANNOTATION_TAG
import net.primal.android.notes.feed.model.asNoteNostrUriUi
import net.primal.android.notes.feed.model.computeRenderedNoteContent
import net.primal.android.notes.feed.model.toAnnotatedString
import net.primal.android.notes.feed.note.ui.attachment.NoteAttachments
import net.primal.android.notes.feed.note.ui.events.InvoicePayClickEvent
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.stream.player.LocalStreamState
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.domain.links.EventUriNostrType
import net.primal.domain.links.ReferencedNote
import net.primal.domain.links.ReferencedUser
import net.primal.domain.nostr.NostrEventKind

internal const val NOT_FOUND_NOTICE_CUT_OFF_LEVEL = 2

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteContent(
    modifier: Modifier = Modifier,
    data: NoteContentUi,
    rendered: RenderedNoteContent? = null,
    expanded: Boolean,
    noteCallbacks: NoteCallbacks,
    nestingLevel: Int = 0,
    nestingCutOffLimit: Int = Int.MAX_VALUE,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textSelectable: Boolean = false,
    referencedEventsHaveBorder: Boolean = true,
    couldAutoPlay: Boolean = false,
    highlightColor: Color = AppTheme.colorScheme.secondary,
    contentColor: Color = AppTheme.colorScheme.onSurface,
    referencedEventsContainerColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
    onClick: ((offset: Offset) -> Unit)? = null,
    onUrlClick: ((url: String) -> Unit)? = null,
    onVideoSoundToggle: ((soundOn: Boolean) -> Unit)? = null,
    onPollOptionSelected: ((optionId: String) -> Unit)? = null,
) {
    val isDarkTheme = LocalPrimalTheme.current.isDarkTheme
    val displaySettings = LocalContentDisplaySettings.current
    val seeMoreText = stringResource(id = R.string.feed_see_more)
    val contentText = remember(data, rendered, expanded, seeMoreText, highlightColor) {
        rendered?.toAnnotatedString(seeMoreText = seeMoreText, highlightColor = highlightColor)
            ?: renderContentAsAnnotatedString(
                data = data,
                expanded = expanded,
                seeMoreText = seeMoreText,
                highlightColor = highlightColor,
            )
    }

    Column(modifier = modifier) {
        if (contentText.isNotEmpty()) {
            val clickHandler = remember(contentText, noteCallbacks, onUrlClick, onClick) {
                { position: Int, offset: Offset ->
                    val annotation = contentText.getStringAnnotations(
                        start = position,
                        end = position,
                    ).firstOrNull()

                    annotation?.handleAnnotationClick(
                        onProfileClick = noteCallbacks.onProfileClick,
                        onUrlClick = {
                            if (it.isPrimalLegendsUrl()) {
                                noteCallbacks.onPrimalLegendsLeaderboardClick?.invoke()
                            } else {
                                onUrlClick?.invoke(it)
                            }
                        },
                        onPostClick = noteCallbacks.onNoteClick,
                        onHashtagClick = noteCallbacks.onHashtagClick,
                        onArticleClick = noteCallbacks.onArticleClick,
                    ) ?: onClick?.invoke(offset) ?: Unit
                }
            }

            PrimalClickableText(
                modifier = Modifier.padding(bottom = 4.dp),
                style = AppTheme.typography.bodyMedium.copy(
                    color = contentColor,
                    fontSize = displaySettings.contentAppearance.noteBodyFontSize,
                    lineHeight = displaySettings.contentAppearance.noteBodyLineHeight,
                ),
                text = contentText,
                maxLines = maxLines,
                overflow = overflow,
                textSelectable = textSelectable,
                onClick = clickHandler,
            )
        }

        val referencedStreams = data.partitions.referencedStreams

        if (referencedStreams.isNotEmpty()) {
            val streamState = LocalStreamState.current

            referencedStreams.forEachIndexed { index, stream ->
                ReferencedStream(
                    stream = stream,
                    onClick = { streamState.start(it) },
                    onProfileClick = { noteCallbacks.onProfileClick?.invoke(it) },
                )

                if (index < referencedStreams.size - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        val referencedHighlights = data.partitions.referencedHighlights
        if (referencedHighlights.isNotEmpty()) {
            referencedHighlights.forEachIndexed { index, highlight ->
                ReferencedHighlight(
                    highlight = highlight,
                    isDarkTheme = isDarkTheme,
                    onClick = { naddr -> noteCallbacks.onArticleClick?.invoke(naddr) },
                )

                if (index < referencedHighlights.size - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        if (data.invoices.isNotEmpty()) {
            NoteLightningInvoice(
                modifier = Modifier.padding(top = if (contentText.isEmpty()) 4.dp else 6.dp),
                invoice = data.invoices.first(),
                onPayClick = { lnbc -> noteCallbacks.onPayInvoiceClick?.invoke(InvoicePayClickEvent(lnbc = lnbc)) },
            )
        }

        if (data.uris.isNotEmpty()) {
            NoteAttachments(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (contentText.isEmpty()) 4.dp else 6.dp)
                    .heightIn(min = 0.dp, max = 500.dp),
                eventUris = data.partitions.filteredEventUris,
                blossoms = data.blossoms,
                expanded = expanded,
                couldAutoPlay = couldAutoPlay,
                onUrlClick = { url ->
                    when {
                        url.isPrimalLegendsUrl() -> noteCallbacks.onPrimalLegendsLeaderboardClick?.invoke()
                        else -> onUrlClick?.invoke(url)
                    }
                },
                onMediaClick = noteCallbacks.onMediaClick,
                onVideoSoundToggle = onVideoSoundToggle,
            )
        }

        val referencedPostResources = data.partitions.referencedNotes
        if (referencedPostResources.isNotEmpty() && (nestingLevel < nestingCutOffLimit || expanded)) {
            ReferencedNotesColumn(
                modifier = Modifier.padding(top = 4.dp),
                nestingLevel = nestingLevel,
                nestingCutOffLimit = nestingCutOffLimit,
                postResources = referencedPostResources,
                expanded = expanded,
                containerColor = referencedEventsContainerColor,
                noteCallbacks = noteCallbacks,
                hasBorder = referencedEventsHaveBorder,
                couldAutoPlay = couldAutoPlay,
                onVideoSoundToggle = onVideoSoundToggle,
            )
        }

        val referencedArticleResources = data.partitions.referencedArticles
        if (referencedArticleResources.isNotEmpty()) {
            ReferencedArticlesColumn(
                modifier = Modifier.padding(top = 4.dp),
                articleResources = referencedArticleResources,
                expanded = expanded,
                containerColor = referencedEventsContainerColor,
                noteCallbacks = noteCallbacks,
                hasBorder = referencedEventsHaveBorder,
            )
        }

        val referencedZaps = data.partitions.referencedZaps
        referencedZaps.forEach { zap ->
            val zappedEventId = zap.zappedEventId
            val zappedEventContent = zap.zappedEventContent
            if (zappedEventId != null && zappedEventContent?.isNotEmpty() == true) {
                ReferencedNoteZap(
                    senderId = zap.senderId,
                    receiverId = zap.receiverId,
                    noteContentUi = NoteContentUi(
                        noteId = zappedEventId,
                        content = zappedEventContent,
                        nostrUris = zap.zappedEventNostrUris.map { it.asNoteNostrUriUi() },
                        hashtags = zap.zappedEventHashtags,
                    ),
                    amountInSats = zap.amountInSats.toULong(),
                    createdAt = Instant.ofEpochSecond(zap.createdAt),
                    noteCallbacks = noteCallbacks,
                    message = zap.message,
                    senderAvatarCdnImage = zap.senderAvatarCdnImage,
                    senderLegendaryCustomization = zap.senderPrimalLegendProfile?.asLegendaryCustomization(),
                    receiverDisplayName = zap.receiverDisplayName,
                    receiverAvatarCdnImage = zap.receiverAvatarCdnImage,
                    receiverLegendaryCustomization = zap.senderPrimalLegendProfile?.asLegendaryCustomization(),
                )
            } else {
                ReferencedZap(
                    senderId = zap.senderId,
                    senderAvatarCdnImage = zap.senderAvatarCdnImage,
                    senderPrimalLegendProfile = zap.senderPrimalLegendProfile,
                    receiverId = zap.receiverId,
                    receiverDisplayName = zap.receiverDisplayName,
                    receiverAvatarCdnImage = zap.receiverAvatarCdnImage,
                    receiverPrimalLegendProfile = zap.receiverPrimalLegendProfile,
                    amountInSats = zap.amountInSats,
                    message = zap.message,
                    noteCallbacks = noteCallbacks,
                )
            }
        }

        val genericEvents = data.partitions.unsupportedEvents
        if (genericEvents.isNotEmpty() && (nestingLevel < NOT_FOUND_NOTICE_CUT_OFF_LEVEL)) {
            genericEvents.forEachIndexed { index, nostrUriUi ->
                NoteUnknownEvent(
                    modifier = Modifier.fillMaxWidth(),
                    icon = nostrUriUi.uri.nostrUriToMissingEventIcon(),
                    altDescription = nostrUriUi.referencedEventAlt
                        ?: nostrUriUi.uri.nostrUriToMissingEventAltDescription(),
                )

                if (index < genericEvents.size - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        if (data.poll != null) {
            NotePollContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (contentText.isEmpty()) 4.dp else 6.dp),
                poll = data.poll,
                onOptionSelected = { optionId -> onPollOptionSelected?.invoke(optionId) },
                onVotesClick = noteCallbacks.onPollVotesClick?.let { callback ->
                    { callback(data.noteId) }
                },
            )
        }
    }
}

@Composable
private fun String.nostrUriToMissingEventAltDescription(): String {
    return if (contains("note1")) {
        stringResource(R.string.feed_missing_event_alt_description_note)
    } else {
        stringResource(R.string.feed_missing_event_alt_description_event)
    }
}

@Composable
private fun String.nostrUriToMissingEventIcon(): ImageVector {
    return if (contains("note1")) {
        Icons.Outlined.ErrorOutline
    } else {
        PrimalIcons.Document
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
): AnnotatedString =
    computeRenderedNoteContent(
        data = data,
        expanded = expanded,
        shouldKeepNostrNoteUris = shouldKeepNostrNoteUris,
    ).toAnnotatedString(seeMoreText = seeMoreText, highlightColor = highlightColor)

private const val PRIMAL_LEGENDS_URL = "primal.net/legends"

private fun String.isPrimalLegendsUrl(): Boolean {
    return this.endsWith(PRIMAL_LEGENDS_URL)
}

@Preview
@Composable
fun PreviewPostContent() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            NoteContent(
                data = NoteContentUi(
                    noteId = "",
                    content = """
                        Hey there nostr:referencedUser, how is life? #nostr 
                    """.trimIndent(),
                    uris = emptyList(),
                    nostrUris = listOf(
                        NoteNostrUriUi(
                            uri = "nostr:referencedUser",
                            type = EventUriNostrType.Profile,
                            referencedEventAlt = null,
                            referencedNote = null,
                            referencedUser = ReferencedUser(
                                userId = "nostr:referencedUser",
                                handle = "alex",
                            ),
                            referencedArticle = null,
                            referencedHighlight = null,
                            referencedZap = null,
                            referencedStream = null,
                            position = 0,
                        ),
                    ),
                    hashtags = listOf("#nostr"),
                ),
                expanded = false,
                onClick = {},
                onUrlClick = {},
                noteCallbacks = NoteCallbacks(),
            )
        }
    }
}

@Preview
@Composable
fun PreviewPostUnknownReferencedEventWithAlt() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            NoteContent(
                data = NoteContentUi(
                    noteId = "",
                    content = "This is amazing! nostr:nevent124124124214123412",
                    uris = emptyList(),
                    nostrUris = listOf(
                        NoteNostrUriUi(
                            uri = "nostr:nevent124124124214123412",
                            type = EventUriNostrType.Unsupported,
                            referencedEventAlt = "This is a music song.",
                            referencedNote = null,
                            referencedUser = null,
                            referencedArticle = null,
                            referencedHighlight = null,
                            referencedZap = null,
                            referencedStream = null,
                            position = 0,
                        ),
                    ),
                    hashtags = listOf("#nostr"),
                ),
                expanded = false,
                onClick = {},
                onUrlClick = {},
                noteCallbacks = NoteCallbacks(),
            )
        }
    }
}

@Preview
@Composable
fun PreviewPostUnknownReferencedEventWithoutAlt() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            NoteContent(
                data = NoteContentUi(
                    noteId = "",
                    content = "This is amazing! nostr:note111",
                    uris = emptyList(),
                    nostrUris = listOf(
                        NoteNostrUriUi(
                            uri = "nostr:note111",
                            type = EventUriNostrType.Unsupported,
                            referencedEventAlt = null,
                            referencedNote = null,
                            referencedUser = null,
                            referencedArticle = null,
                            referencedHighlight = null,
                            referencedZap = null,
                            referencedStream = null,
                            position = 0,
                        ),
                    ),
                    hashtags = listOf("#nostr"),
                ),
                expanded = false,
                onClick = {},
                onUrlClick = {},
                noteCallbacks = NoteCallbacks(),
            )
        }
    }
}

@Preview
@Composable
@Suppress("LongMethod")
fun PreviewPostContentWithReferencedPost() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
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
                    uris = emptyList(),
                    nostrUris = listOf(
                        NoteNostrUriUi(
                            uri = "nostr:referencedPost",
                            type = EventUriNostrType.Note,
                            referencedNote = ReferencedNote(
                                postId = "postId",
                                kind = NostrEventKind.ShortTextNote.value,
                                createdAt = 0,
                                content = "This is referenced post.",
                                authorId = "authorId",
                                authorName = "primal",
                                authorAvatarCdnImage = null,
                                authorInternetIdentifier = "hi@primal.net",
                                authorLightningAddress = "h@getalby.com",
                                attachments = emptyList(),
                                nostrUris = emptyList(),
                                authorLegendProfile = null,
                                raw = "",
                            ),
                            referencedUser = null,
                            referencedArticle = null,
                            referencedEventAlt = null,
                            referencedHighlight = null,
                            referencedZap = null,
                            referencedStream = null,
                            position = 0,
                        ),
                        NoteNostrUriUi(
                            uri = "nostr:referenced2Post",
                            type = EventUriNostrType.Note,
                            referencedNote = ReferencedNote(
                                postId = "postId",
                                kind = NostrEventKind.ShortTextNote.value,
                                createdAt = 0,
                                content = "This is referenced post #2.",
                                authorId = "authorId",
                                authorName = "primal",
                                authorAvatarCdnImage = null,
                                authorInternetIdentifier = "hi@primal.net",
                                authorLightningAddress = "h@getalby.com",
                                attachments = emptyList(),
                                nostrUris = emptyList(),
                                authorLegendProfile = null,
                                raw = "",
                            ),
                            referencedUser = null,
                            referencedArticle = null,
                            referencedEventAlt = null,
                            referencedHighlight = null,
                            referencedZap = null,
                            referencedStream = null,
                            position = 1,
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
                onClick = {},
                onUrlClick = {},
                noteCallbacks = NoteCallbacks(),
            )
        }
    }
}

package net.primal.android.notes.feed.note

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.profile.approvals.ApproveBookmarkAlertDialog
import net.primal.android.core.errors.UiError
import net.primal.android.core.ext.openUriSafely
import net.primal.android.events.domain.CdnImage
import net.primal.android.notes.feed.NoteRepostOrQuoteBottomSheet
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostAction
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.toNoteContentUi
import net.primal.android.notes.feed.note.NoteContract.UiEvent
import net.primal.android.notes.feed.note.ui.FeedNoteActionsRow
import net.primal.android.notes.feed.note.ui.FeedNoteHeader
import net.primal.android.notes.feed.note.ui.NoteContent
import net.primal.android.notes.feed.note.ui.NoteDropdownMenuIcon
import net.primal.android.notes.feed.note.ui.NoteSurfaceCard
import net.primal.android.notes.feed.note.ui.RepostedNotice
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.notes.feed.zaps.UnableToZapBottomSheet
import net.primal.android.notes.feed.zaps.ZapBottomSheet
import net.primal.android.profile.report.ReportUserDialog
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.wallet.zaps.canZap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedNoteCard(
    data: FeedPostUi,
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = noteCardColors(),
    cardPadding: PaddingValues = PaddingValues(all = 0.dp),
    enableTweetsMode: Boolean = false,
    headerSingleLine: Boolean = true,
    fullWidthContent: Boolean = false,
    forceContentIndent: Boolean = false,
    drawLineAboveAvatar: Boolean = false,
    drawLineBelowAvatar: Boolean = false,
    expanded: Boolean = false,
    textSelectable: Boolean = false,
    showReplyTo: Boolean = true,
    noteOptionsMenuEnabled: Boolean = true,
    showNoteStatCounts: Boolean = true,
    noteCallbacks: NoteCallbacks = NoteCallbacks(),
    onGoToWallet: (() -> Unit)? = null,
    onUiError: ((UiError) -> Unit)? = null,
    contentFooter: @Composable () -> Unit = {},
) {
    val viewModel = hiltViewModel<NoteViewModel, NoteViewModel.Factory> { it.create(noteId = data.postId) }
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(viewModel, uiState.error, onUiError) {
        uiState.error?.let { onUiError?.invoke(it) }
        viewModel.setEvent(UiEvent.DismissError)
    }

    FeedNoteCard(
        data = data,
        state = uiState,
        eventPublisher = viewModel::setEvent,
        modifier = modifier,
        shape = shape,
        colors = colors,
        cardPadding = cardPadding,
        enableTweetsMode = enableTweetsMode,
        headerSingleLine = headerSingleLine,
        fullWidthContent = fullWidthContent,
        forceContentIndent = forceContentIndent,
        drawLineAboveAvatar = drawLineAboveAvatar,
        drawLineBelowAvatar = drawLineBelowAvatar,
        expanded = expanded,
        textSelectable = textSelectable,
        showReplyTo = showReplyTo,
        showNoteStatCounts = showNoteStatCounts,
        noteOptionsMenuEnabled = noteOptionsMenuEnabled,
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
        contentFooter = contentFooter,
    )
}

@ExperimentalMaterial3Api
@Composable
private fun FeedNoteCard(
    data: FeedPostUi,
    state: NoteContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = noteCardColors(),
    cardPadding: PaddingValues = PaddingValues(all = 0.dp),
    enableTweetsMode: Boolean = false,
    headerSingleLine: Boolean = true,
    fullWidthContent: Boolean = false,
    forceContentIndent: Boolean = false,
    drawLineAboveAvatar: Boolean = false,
    drawLineBelowAvatar: Boolean = false,
    expanded: Boolean = false,
    textSelectable: Boolean = false,
    showReplyTo: Boolean = true,
    noteOptionsMenuEnabled: Boolean = true,
    showNoteStatCounts: Boolean = true,
    noteCallbacks: NoteCallbacks = NoteCallbacks(),
    onGoToWallet: (() -> Unit)? = null,
    contentFooter: @Composable () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }

    var showCantZapWarning by remember { mutableStateOf(false) }
    if (showCantZapWarning) {
        UnableToZapBottomSheet(
            zappingState = state.zappingState,
            onDismissRequest = { showCantZapWarning = false },
            onGoToWallet = { onGoToWallet?.invoke() },
        )
    }

    var showZapOptions by remember { mutableStateOf(false) }
    if (showZapOptions) {
        ZapBottomSheet(
            onDismissRequest = { showZapOptions = false },
            receiverName = data.authorName,
            zappingState = state.zappingState,
            onZap = { zapAmount, zapDescription ->
                if (state.zappingState.canZap(zapAmount)) {
                    eventPublisher(
                        UiEvent.ZapAction(
                            postId = data.postId,
                            postAuthorId = data.authorId,
                            zapAmount = zapAmount.toULong(),
                            zapDescription = zapDescription,
                        ),
                    )
                } else {
                    showCantZapWarning = true
                }
            },
        )
    }

    var reportDialogVisible by remember { mutableStateOf(false) }
    if (reportDialogVisible) {
        ReportUserDialog(
            onDismissRequest = { reportDialogVisible = false },
            onReportClick = { type ->
                reportDialogVisible = false
                eventPublisher(
                    UiEvent.ReportAbuse(
                        reportType = type,
                        profileId = data.authorId,
                        noteId = data.postId,
                    ),
                )
            },
        )
    }

    var showRepostOrQuoteConfirmation by remember { mutableStateOf(false) }
    if (showRepostOrQuoteConfirmation) {
        NoteRepostOrQuoteBottomSheet(
            onDismiss = { showRepostOrQuoteConfirmation = false },
            onRepostClick = {
                eventPublisher(
                    UiEvent.RepostAction(
                        postId = data.postId,
                        postAuthorId = data.authorId,
                        postNostrEvent = data.rawNostrEventJson,
                    ),
                )
            },
            onPostQuoteClick = { noteCallbacks.onNoteQuoteClick?.invoke(data.postId) },
        )
    }

    if (state.shouldApproveBookmark) {
        ApproveBookmarkAlertDialog(
            onBookmarkConfirmed = {
                eventPublisher(
                    UiEvent.BookmarkAction(
                        noteId = data.postId,
                        forceUpdate = true,
                    ),
                )
            },
            onClose = {
                eventPublisher(UiEvent.DismissBookmarkConfirmation(noteId = data.postId))
            },
        )
    }

    val displaySettings = LocalContentDisplaySettings.current
    val notePaddingDp = 4.dp
    val avatarPaddingDp = 8.dp
    val avatarSizeDp = displaySettings.contentAppearance.noteAvatarSize
    val overflowIconSizeDp = 40.dp

    val graphicsLayer = rememberGraphicsLayer()

    NoteSurfaceCard(
        modifier = modifier
            .wrapContentHeight()
            .padding(cardPadding)
            .clickable(
                enabled = noteCallbacks.onNoteClick != null,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = { noteCallbacks.onNoteClick?.invoke(data.postId) },
            ),
        shape = shape,
        colors = colors,
        drawLineAboveAvatar = drawLineAboveAvatar,
        drawLineBelowAvatar = drawLineBelowAvatar,
        lineOffsetX = (avatarSizeDp / 2) + avatarPaddingDp + notePaddingDp + 2.dp,
    ) {
        Box(
            modifier = Modifier.padding(all = notePaddingDp),
            contentAlignment = Alignment.TopEnd,
        ) {
            NoteDropdownMenuIcon(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(overflowIconSizeDp)
                    .padding(
                        top = when {
                            data.repostAuthorName != null -> 4.dp
                            !fullWidthContent -> 6.dp
                            else -> 13.dp
                        },
                    )
                    .clip(CircleShape)
                    .zIndex(1f),
                noteId = data.postId,
                noteContent = data.content,
                noteRawData = data.rawNostrEventJson,
                authorId = data.authorId,
                isBookmarked = data.isBookmarked,
                relayHints = state.relayHints,
                enabled = noteOptionsMenuEnabled,
                noteGraphicsLayer = graphicsLayer,
                onBookmarkClick = {
                    eventPublisher(UiEvent.BookmarkAction(noteId = data.postId))
                },
                onMuteUserClick = {
                    eventPublisher(UiEvent.MuteAction(userId = data.authorId))
                },
                onReportContentClick = {
                    reportDialogVisible = true
                },
            )

            Column {
                if (data.repostAuthorName != null) {
                    RepostedNotice(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = avatarPaddingDp)
                            .padding(top = 4.dp),
                        repostedByAuthor = data.repostAuthorName,
                        onRepostAuthorClick = if (data.repostAuthorId != null && noteCallbacks.onProfileClick != null) {
                            { noteCallbacks.onProfileClick.invoke(data.repostAuthorId) }
                        } else {
                            null
                        },
                    )
                }

                Column(
                    modifier = Modifier
                        .drawWithContent {
                            graphicsLayer.record { this@drawWithContent.drawContent() }
                            drawLayer(graphicsLayer)
                        }
                        .clip(
                            RoundedCornerShape(
                                topStart = AppTheme.shapes.medium.topStart,
                                bottomStart = AppTheme.shapes.medium.bottomStart,
                                topEnd = AppTheme.shapes.medium.topEnd,
                                bottomEnd = AppTheme.shapes.medium.bottomEnd,
                            ),
                        )
                        .background(color = AppTheme.colorScheme.surfaceVariant),
                ) {
                    FeedNote(
                        data = data,
                        fullWidthContent = fullWidthContent,
                        avatarSizeDp = avatarSizeDp,
                        avatarPaddingValues = PaddingValues(start = avatarPaddingDp, top = avatarPaddingDp),
                        notePaddingValues = PaddingValues(
                            start = 8.dp,
                            top = avatarPaddingDp,
                            end = overflowIconSizeDp - 8.dp,
                        ),
                        enableTweetsMode = enableTweetsMode,
                        headerSingleLine = headerSingleLine,
                        showReplyTo = showReplyTo,
                        forceContentIndent = forceContentIndent,
                        expanded = expanded,
                        textSelectable = textSelectable,
                        showNoteStatCounts = showNoteStatCounts,
                        noteCallbacks = noteCallbacks,
                        onPostAction = { postAction ->
                            when (postAction) {
                                FeedPostAction.Reply -> {
                                    noteCallbacks.onNoteReplyClick?.invoke(data.postId)
                                }

                                FeedPostAction.Zap -> {
                                    if (state.zappingState.canZap()) {
                                        eventPublisher(
                                            UiEvent.ZapAction(
                                                postId = data.postId,
                                                postAuthorId = data.authorId,
                                            ),
                                        )
                                    } else {
                                        showCantZapWarning = true
                                    }
                                }

                                FeedPostAction.Like -> {
                                    eventPublisher(
                                        UiEvent.PostLikeAction(
                                            postId = data.postId,
                                            postAuthorId = data.authorId,
                                        ),
                                    )
                                }

                                FeedPostAction.Repost -> {
                                    showRepostOrQuoteConfirmation = true
                                }

                                FeedPostAction.Bookmark -> {
                                    eventPublisher(UiEvent.BookmarkAction(noteId = data.postId))
                                }
                            }
                        },
                        onPostLongClickAction = { postAction ->
                            when (postAction) {
                                FeedPostAction.Zap -> {
                                    if (state.zappingState.walletConnected) {
                                        showZapOptions = true
                                    } else {
                                        showCantZapWarning = true
                                    }
                                }

                                else -> Unit
                            }
                        },
                        contentFooter = contentFooter,
                    )
                }
            }
        }
    }
}

@Composable
private fun noteCardColors() =
    CardDefaults.cardColors(
        containerColor = AppTheme.colorScheme.surfaceVariant,
    )

@Composable
private fun FeedNote(
    data: FeedPostUi,
    fullWidthContent: Boolean,
    avatarSizeDp: Dp,
    avatarPaddingValues: PaddingValues,
    notePaddingValues: PaddingValues,
    enableTweetsMode: Boolean,
    headerSingleLine: Boolean,
    showReplyTo: Boolean,
    forceContentIndent: Boolean,
    expanded: Boolean,
    textSelectable: Boolean,
    showNoteStatCounts: Boolean,
    noteCallbacks: NoteCallbacks,
    onPostAction: ((FeedPostAction) -> Unit)? = null,
    onPostLongClickAction: ((FeedPostAction) -> Unit)? = null,
    contentFooter: @Composable () -> Unit = {},
) {
    val localUriHandler = LocalUriHandler.current
    val uiScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    Row {
        if (!fullWidthContent) {
            UniversalAvatarThumbnail(
                modifier = Modifier.padding(avatarPaddingValues),
                avatarSize = avatarSizeDp,
                avatarCdnImage = data.authorAvatarCdnImage,
                legendaryCustomization = data.authorLegendaryCustomization,
                onClick = if (noteCallbacks.onProfileClick != null) {
                    { noteCallbacks.onProfileClick.invoke(data.authorId) }
                } else {
                    null
                },
            )
        }

        Column(
            modifier = Modifier.padding(start = 0.dp),
        ) {
            FeedNoteHeader(
                modifier = Modifier
                    .padding(notePaddingValues)
                    .padding(end = 4.dp)
                    .fillMaxWidth(),
                postTimestamp = data.timestamp,
                singleLine = headerSingleLine,
                authorAvatarVisible = fullWidthContent,
                authorAvatarSize = avatarSizeDp,
                authorDisplayName = data.authorName,
                authorAvatarCdnImage = data.authorAvatarCdnImage,
                authorInternetIdentifier = data.authorInternetIdentifier,
                authorLegendaryCustomization = data.authorLegendaryCustomization,
                replyToAuthor = if (showReplyTo) data.replyToAuthorHandle else null,
                onAuthorAvatarClick = if (noteCallbacks.onProfileClick != null) {
                    { noteCallbacks.onProfileClick.invoke(data.authorId) }
                } else {
                    null
                },
            )

            val postAuthorGuessHeight = with(LocalDensity.current) { 128.dp.toPx() }
            val launchRippleEffect: (Offset) -> Unit = {
                uiScope.launch {
                    val press = PressInteraction.Press(it.copy(y = it.y + postAuthorGuessHeight))
                    interactionSource.emit(press)
                    interactionSource.emit(PressInteraction.Release(press))
                }
            }

            NoteContent(
                modifier = Modifier
                    .padding(horizontal = if (fullWidthContent) 10.dp else 8.dp)
                    .padding(start = if (forceContentIndent && fullWidthContent) avatarSizeDp + 6.dp else 0.dp)
                    .padding(top = if (fullWidthContent || !headerSingleLine) 10.dp else 5.dp),
                data = data.toNoteContentUi(),
                expanded = expanded,
                enableTweetsMode = enableTweetsMode,
                textSelectable = textSelectable,
                onClick = if (noteCallbacks.onNoteClick != null) {
                    {
                        launchRippleEffect(it)
                        noteCallbacks.onNoteClick.invoke(data.postId)
                    }
                } else {
                    null
                },
                onUrlClick = { localUriHandler.openUriSafely(it) },
                noteCallbacks = noteCallbacks,
            )

            contentFooter()

            if (!showNoteStatCounts) {
                PrimalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            FeedNoteActionsRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (showNoteStatCounts) 12.dp else 16.dp)
                    .padding(vertical = if (showNoteStatCounts) 8.dp else 12.dp)
                    .padding(top = if (showNoteStatCounts) 4.dp else 0.dp),
                eventStats = data.stats,
                showCounts = showNoteStatCounts,
                highlightedNote = !showNoteStatCounts,
                isBookmarked = data.isBookmarked,
                onPostAction = onPostAction,
                onPostLongPressAction = onPostLongClickAction,
                showBookmark = fullWidthContent,
            )
        }
    }
}

class FeedPostUiProvider : PreviewParameterProvider<FeedPostUi> {
    override val values: Sequence<FeedPostUi>
        get() = sequenceOf(
            FeedPostUi(
                postId = "random",
                content = """
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                """.trimIndent(),
                uris = emptyList(),
                authorId = "npubSomething",
                authorName = "android_robots_from_space",
                authorHandle = "user",
                authorInternetIdentifier = "android@primal.net",
                authorAvatarCdnImage = CdnImage(sourceUrl = "https://i.imgur.com/Z8dpmvc.png"),
                timestamp = Instant.now().minus(30, ChronoUnit.MINUTES),
                nostrUris = emptyList(),
                stats = EventStatsUi(
                    repliesCount = 0,
                    likesCount = 0,
                    userLiked = false,
                    repostsCount = 0,
                    satsZapped = 0,
                ),
                hashtags = listOf("#nostr"),
                rawNostrEventJson = "",
                replyToAuthorHandle = "alex",
            ),
            FeedPostUi(
                postId = "random",
                repostId = "repostRandom",
                repostAuthorId = "repostId",
                repostAuthorName = "jack",
                content = """
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi et ligula feugiat enim faucibus volutpat ac et lorem. Vivamus tellus sem, rutrum non elit sed, congue efficitur nisl. 
                """.trimIndent(),
                uris = emptyList(),
                authorId = "npubSomething",
                authorName = "android_robots_from_space",
                authorHandle = "user",
                authorInternetIdentifier = "android@primal.net",
                authorAvatarCdnImage = CdnImage(sourceUrl = "https://i.imgur.com/Z8dpmvc.png"),
                timestamp = Instant.now().minus(30, ChronoUnit.MINUTES),
                nostrUris = emptyList(),
                stats = EventStatsUi(
                    repliesCount = 11,
                    likesCount = 256,
                    userLiked = true,
                    repostsCount = 42,
                    satsZapped = 555,
                ),
                hashtags = listOf("#nostr"),
                rawNostrEventJson = "",
                replyToAuthorHandle = null,
            ),
            FeedPostUi(
                postId = "random",
                repostId = "repostRandom",
                content = """
                    Curabitur convallis ante eget mi facilisis, vitae egestas libero accumsan.
                    
                    Duis ante turpis, dapibus vitae est vehicula, vulputate commodo libero. Vestibulum massa ante, semper sit amet urna vel, commodo maximus velit. Sed nec nisi laoreet, faucibus est ac, placerat orci. 
                """.trimIndent(),
                uris = emptyList(),
                authorId = "npubSomething",
                authorName = "primal",
                authorHandle = "primal",
                authorInternetIdentifier = "primal@primal.net",
                authorAvatarCdnImage = CdnImage(sourceUrl = "https://i.imgur.com/Z8dpmvc.png"),
                timestamp = Instant.now().minus(30, ChronoUnit.MINUTES),
                nostrUris = emptyList(),
                stats = EventStatsUi(
                    repliesCount = 11,
                    likesCount = 256,
                    userLiked = true,
                    repostsCount = 42,
                    satsZapped = 555,
                ),
                hashtags = listOf("#nostr"),
                rawNostrEventJson = "",
                replyToAuthorHandle = null,
            ),
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewFeedNoteListItemLightMultiLineHeader(
    @PreviewParameter(FeedPostUiProvider::class)
    feedPostUi: FeedPostUi,
) {
    PrimalPreview(primalTheme = PrimalTheme.Sunrise) {
        FeedNoteCard(
            data = feedPostUi,
            state = NoteContract.UiState(),
            eventPublisher = {},
            headerSingleLine = false,
            fullWidthContent = false,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewFeedNoteListItemLightMultiLineHeaderFullWidth(
    @PreviewParameter(FeedPostUiProvider::class)
    feedPostUi: FeedPostUi,
) {
    PrimalPreview(primalTheme = PrimalTheme.Sunrise) {
        FeedNoteCard(
            data = feedPostUi,
            state = NoteContract.UiState(),
            eventPublisher = {},
            headerSingleLine = false,
            fullWidthContent = true,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewFeedNoteListItemDarkSingleLineHeader(
    @PreviewParameter(FeedPostUiProvider::class)
    feedPostUi: FeedPostUi,
) {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        FeedNoteCard(
            data = feedPostUi,
            state = NoteContract.UiState(),
            eventPublisher = {},
            headerSingleLine = true,
            fullWidthContent = false,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewFeedNoteListItemDarkSingleLineHeaderFullWidth(
    @PreviewParameter(FeedPostUiProvider::class)
    feedPostUi: FeedPostUi,
) {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        FeedNoteCard(
            data = feedPostUi,
            state = NoteContract.UiState(),
            eventPublisher = {},
            headerSingleLine = true,
            fullWidthContent = true,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewFeedNoteListItemLightForcedContentIndentFullWidthSingleLineHeader(
    @PreviewParameter(FeedPostUiProvider::class)
    feedPostUi: FeedPostUi,
) {
    PrimalPreview(primalTheme = PrimalTheme.Sunrise) {
        FeedNoteCard(
            data = feedPostUi,
            state = NoteContract.UiState(),
            eventPublisher = {},
            headerSingleLine = true,
            fullWidthContent = true,
            forceContentIndent = true,
            drawLineBelowAvatar = true,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewFeedNoteListItemDarkForcedContentIndentSingleLineHeader(
    @PreviewParameter(FeedPostUiProvider::class)
    feedPostUi: FeedPostUi,
) {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        FeedNoteCard(
            data = feedPostUi,
            state = NoteContract.UiState(),
            eventPublisher = {},
            headerSingleLine = true,
            fullWidthContent = false,
            forceContentIndent = true,
            drawLineBelowAvatar = true,
        )
    }
}

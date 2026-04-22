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
import androidx.compose.foundation.shape.CornerSize
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.layer.GraphicsLayer
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
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch
import net.primal.android.core.activity.LocalContentDisplaySettings
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.errors.UiError
import net.primal.android.core.ext.openUriSafely
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostAction
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.PollOptionUi
import net.primal.android.notes.feed.model.PollState
import net.primal.android.notes.feed.model.PollType
import net.primal.android.notes.feed.model.PollUi
import net.primal.android.notes.feed.model.asNeventString
import net.primal.android.notes.feed.model.toNoteContentUi
import net.primal.android.notes.feed.note.NoteContract.UiEvent
import net.primal.android.notes.feed.note.ui.FeedNoteActionsRow
import net.primal.android.notes.feed.note.ui.FeedNoteHeader
import net.primal.android.notes.feed.note.ui.NoteContent
import net.primal.android.notes.feed.note.ui.NoteDropdownMenuIcon
import net.primal.android.notes.feed.note.ui.NoteSurfaceCard
import net.primal.android.notes.feed.note.ui.RepostedNotice
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.notes.feed.zaps.ZapPollBottomSheet
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.domain.links.CdnImage
import net.primal.domain.utils.canZap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedNoteCard(
    data: FeedPostUi,
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = noteCardColors(),
    cardPadding: PaddingValues = PaddingValues(all = 0.dp),
    nestingCutOffLimit: Int = Int.MAX_VALUE,
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
    couldAutoPlay: Boolean = false,
    noteCallbacks: NoteCallbacks = NoteCallbacks(),
    onNoteDeleted: (() -> Unit)? = null,
    onGoToWallet: (() -> Unit)? = null,
    onUiError: ((UiError) -> Unit)? = null,
    contentFooter: @Composable () -> Unit = {},
) {
    val viewModel = hiltViewModel<NoteViewModel, NoteViewModel.Factory>(
        key = "noteViewModel$${data.postId}",
        creationCallback = { it.create(noteId = data.postId) },
    )
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(viewModel, uiState.error, onUiError) {
        uiState.error?.let { onUiError?.invoke(it) }
        viewModel.setEvent(UiEvent.DismissError)
    }

    LaunchedEffect(viewModel, onNoteDeleted) {
        viewModel.effects.collect {
            when (it) {
                NoteContract.SideEffect.NoteDeleted -> onNoteDeleted?.invoke()
            }
        }
    }

    FeedNoteCard(
        data = data,
        state = uiState,
        eventPublisher = viewModel::setEvent,
        modifier = modifier,
        shape = shape,
        colors = colors,
        cardPadding = cardPadding,
        nestingCutOffLimit = nestingCutOffLimit,
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
        couldAutoPlay = couldAutoPlay,
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
    nestingCutOffLimit: Int = Int.MAX_VALUE,
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
    couldAutoPlay: Boolean = false,
    noteCallbacks: NoteCallbacks = NoteCallbacks(),
    onGoToWallet: (() -> Unit)? = null,
    contentFooter: @Composable () -> Unit = {},
) {
    val dialogsState = rememberNoteCardDialogsState()
    NoteCardDialogs(
        dialogsState = dialogsState,
        data = data,
        noteState = state,
        eventPublisher = eventPublisher,
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
    )

    val interactionSource = remember { MutableInteractionSource() }

    var zapPollSelectedOptionId by remember { mutableStateOf<String?>(null) }
    val zapPoll = data.poll
    if (zapPollSelectedOptionId != null && zapPoll != null) {
        ZapPollBottomSheet(
            optionId = zapPollSelectedOptionId ?: "",
            valueMinimum = zapPoll.valueMinimum,
            valueMaximum = zapPoll.valueMaximum,
            exchangeRate = state.currentExchangeRate,
            defaultZapAmounts = state.zappingState.zapsConfig.map { it.amount },
            onDismissRequest = { zapPollSelectedOptionId = null },
            onVote = { optionId, amount, comment ->
                eventPublisher(
                    UiEvent.ZapPollVoteAction(
                        postId = data.postId,
                        optionId = optionId,
                        zapAmount = amount,
                        zapComment = comment,
                        poll = zapPoll,
                    ),
                )
            },
        )
    }

    val displaySettings = LocalContentDisplaySettings.current
    val notePaddingDp = 4.dp
    val avatarPaddingDp = 8.dp
    val avatarSizeDp = if (fullWidthContent) {
        displaySettings.contentAppearance.noteAvatarSize
    } else {
        displaySettings.contentAppearance.replyAvatarSize
    }
    val overflowIconSizeDp = 40.dp

    val threadAlignmentPadding = if (!fullWidthContent && (drawLineAboveAvatar || drawLineBelowAvatar)) {
        (displaySettings.contentAppearance.noteAvatarSize - displaySettings.contentAppearance.replyAvatarSize) / 2
    } else {
        0.dp
    }

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
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (fullWidthContent && forceContentIndent) 0.dp else notePaddingDp,
            ),
        ) {
            if (data.repostAuthorName != null) {
                val repostedNoticeStartPadding = if (fullWidthContent) {
                    (avatarSizeDp - 4.dp).coerceAtLeast(avatarPaddingDp)
                } else {
                    (avatarPaddingDp + threadAlignmentPadding + avatarSizeDp - 6.dp)
                        .coerceAtLeast(avatarPaddingDp)
                }
                RepostedNotice(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = repostedNoticeStartPadding, end = avatarPaddingDp)
                        .padding(top = notePaddingDp * 2),
                    repostedByAuthor = data.repostAuthorName,
                    onRepostAuthorClick = if (data.repostAuthorId != null && noteCallbacks.onProfileClick != null) {
                        { noteCallbacks.onProfileClick.invoke(data.repostAuthorId) }
                    } else {
                        null
                    },
                )
            }

            Box(contentAlignment = Alignment.TopEnd) {
                val dropdownTopPadding = when {
                    !fullWidthContent -> 11.dp
                    forceContentIndent -> 14.dp
                    else -> 18.dp
                } - if (data.repostAuthorName != null) notePaddingDp else 0.dp
                NoteDropdownMenuIcon(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(overflowIconSizeDp)
                        .padding(top = dropdownTopPadding)
                        .clip(CircleShape)
                        .zIndex(1f),
                    noteId = data.postId,
                    noteContent = data.content,
                    noteRawData = data.rawNostrEventJson,
                    authorId = data.authorId,
                    isBookmarked = data.isBookmarked,
                    isThreadMuted = data.isThreadMuted,
                    isNoteAuthor = data.authorId == state.activeAccountUserId,
                    isPoll = data.poll != null,
                    relayHints = state.relayHints,
                    enabled = noteOptionsMenuEnabled,
                    noteGraphicsLayer = graphicsLayer,
                    onBookmarkClick = {
                        eventPublisher(UiEvent.BookmarkAction(noteId = data.postId))
                    },
                    onMuteUserClick = {
                        eventPublisher(UiEvent.MuteUserAction(userId = data.authorId))
                    },
                    onMuteThreadClick = {
                        eventPublisher(UiEvent.MuteThreadAction(postId = data.postId))
                    },
                    onUnmuteThreadClick = {
                        eventPublisher(UiEvent.UnmuteThreadAction(postId = data.postId))
                    },
                    onRequestDeleteClick = {
                        dialogsState.showDeleteDialog = true
                    },
                    onReportContentClick = {
                        dialogsState.showReportDialog = true
                    },
                )

                Column(
                    modifier = Modifier
                        .shareableGraphics(
                            graphicsLayer = graphicsLayer,
                            backgroundColor = colors.containerColor,
                            drawLineAboveAvatar = drawLineAboveAvatar,
                            drawLineBelowAvatar = drawLineBelowAvatar,
                            outlineColor = AppTheme.colorScheme.outline,
                            lineOffsetX = avatarPaddingDp + threadAlignmentPadding +
                                avatarSizeDp / 2 + (1.5).dp,
                            lineWidth = 2.dp,
                        )
                        .padding(
                            top = if (data.repostAuthorName == null) notePaddingDp else 0.dp,
                            bottom = notePaddingDp,
                        ),
                ) {
                    val noteData = state.poll?.let { data.copy(poll = it) } ?: data
                    FeedNote(
                        data = noteData,
                        fullWidthContent = fullWidthContent,
                        avatarSizeDp = avatarSizeDp,
                        avatarPaddingValues = PaddingValues(
                            start = avatarPaddingDp + threadAlignmentPadding,
                            top = avatarPaddingDp + 2.dp,
                        ),
                        notePaddingValues = PaddingValues(
                            start = 8.dp,
                            top = avatarPaddingDp,
                            end = overflowIconSizeDp - 8.dp,
                        ),
                        nestingCutOffLimit = nestingCutOffLimit,
                        headerSingleLine = headerSingleLine,
                        showReplyTo = showReplyTo,
                        forceContentIndent = forceContentIndent,
                        expanded = expanded,
                        textSelectable = textSelectable,
                        showNoteStatCounts = showNoteStatCounts,
                        couldAutoPlay = couldAutoPlay,
                        noteCallbacks = noteCallbacks,
                        onPostAction = { postAction ->
                            when (postAction) {
                                FeedPostAction.Reply -> {
                                    noteCallbacks.onNoteReplyClick?.invoke(
                                        data.asNeventString(),
                                    )
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
                                        dialogsState.showCantZapWarning = true
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
                                    dialogsState.showRepostConfirmation = true
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
                                        dialogsState.showZapOptions = true
                                    } else {
                                        dialogsState.showCantZapWarning = true
                                    }
                                }

                                else -> Unit
                            }
                        },
                        onVideoSoundToggle = { soundPref ->
                            eventPublisher(UiEvent.UpdateAutoPlayVideoSoundPreference(soundPref))
                        },
                        contentFooter = contentFooter,
                        onPollOptionSelected = { optionId ->
                            val poll = data.poll ?: return@FeedNote
                            if (poll.pollType == PollType.Zap) {
                                zapPollSelectedOptionId = optionId
                            } else {
                                eventPublisher(
                                    UiEvent.PollVoteAction(
                                        postId = data.postId,
                                        optionId = optionId,
                                        poll = poll,
                                    ),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun Modifier.shareableGraphics(
    graphicsLayer: GraphicsLayer = rememberGraphicsLayer(),
    clip: Shape = RoundedCornerShape(corner = CornerSize(size = 16.dp)),
    outlineColor: Color? = null,
    backgroundColor: Color = AppTheme.colorScheme.surfaceVariant,
    drawLineBelowAvatar: Boolean = false,
    drawLineAboveAvatar: Boolean = false,
    lineOffsetX: Dp = 0.dp,
    lineWidth: Dp = 0.dp,
) = this
    .drawWithContent {
        graphicsLayer.record { this@drawWithContent.drawContent() }
        drawLayer(graphicsLayer)
    }
    .clip(clip)
    .background(color = backgroundColor)
    .drawWithCache {
        onDrawBehind {
            val connectionX = lineOffsetX.toPx()

            if (drawLineBelowAvatar && outlineColor != null) {
                drawLine(
                    color = outlineColor,
                    start = Offset(x = connectionX, y = 16.dp.toPx()),
                    end = Offset(x = connectionX, y = size.height),
                    strokeWidth = lineWidth.toPx(),
                    cap = StrokeCap.Square,
                )
            }

            if (drawLineAboveAvatar && outlineColor != null) {
                drawLine(
                    color = outlineColor,
                    start = Offset(x = connectionX, y = 0f),
                    end = Offset(x = connectionX, y = 16.dp.toPx()),
                    strokeWidth = lineWidth.toPx(),
                    cap = StrokeCap.Square,
                )
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
    headerSingleLine: Boolean,
    showReplyTo: Boolean,
    forceContentIndent: Boolean,
    expanded: Boolean,
    textSelectable: Boolean,
    showNoteStatCounts: Boolean,
    couldAutoPlay: Boolean,
    noteCallbacks: NoteCallbacks,
    nestingCutOffLimit: Int = Int.MAX_VALUE,
    onPostAction: ((FeedPostAction) -> Unit)? = null,
    onPostLongClickAction: ((FeedPostAction) -> Unit)? = null,
    contentFooter: @Composable () -> Unit = {},
    onVideoSoundToggle: ((soundOn: Boolean) -> Unit)? = null,
    onPollOptionSelected: ((optionId: String) -> Unit)? = null,
) {
    val localUriHandler = LocalUriHandler.current
    val uiScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isFeedLayout = fullWidthContent && forceContentIndent
    val contentIndentDp = if (isFeedLayout) avatarSizeDp + 10.dp else 0.dp

    Box {
        Row {
            if (!fullWidthContent) {
                UniversalAvatarThumbnail(
                    modifier = Modifier.padding(avatarPaddingValues),
                    avatarSize = avatarSizeDp,
                    avatarCdnImage = data.authorAvatarCdnImage,
                    legendaryCustomization = data.authorLegendaryCustomization,
                    avatarBlossoms = data.authorBlossoms,
                    onClick = if (noteCallbacks.onProfileClick != null) {
                        { noteCallbacks.onProfileClick.invoke(data.authorId) }
                    } else {
                        null
                    },
                    isLive = data.isAuthorLiveStreamingNow,
                )
            }

            Column(
                modifier = Modifier.padding(start = 0.dp),
            ) {
                FeedNoteHeader(
                    modifier = Modifier
                        .padding(notePaddingValues)
                        .padding(start = if (isFeedLayout) avatarSizeDp + 10.dp else 0.dp)
                        .padding(top = if (isFeedLayout) 3.dp else 0.dp)
                        .padding(end = 4.dp)
                        .fillMaxWidth(),
                    postTimestamp = data.timestamp,
                    singleLine = headerSingleLine,
                    authorAvatarVisible = fullWidthContent && !forceContentIndent,
                    authorAvatarSize = avatarSizeDp,
                    authorDisplayName = data.authorName,
                    authorAvatarCdnImage = data.authorAvatarCdnImage,
                    authorInternetIdentifier = data.authorInternetIdentifier,
                    authorLegendaryCustomization = data.authorLegendaryCustomization,
                    authorBlossoms = data.authorBlossoms,
                    authorId = data.authorId,
                    replyToAuthor = if (showReplyTo) data.replyToAuthorHandle else null,
                    isLive = data.isAuthorLiveStreamingNow,
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
                        .padding(horizontal = if (fullWidthContent && !forceContentIndent) 10.dp else 8.dp)
                        .padding(start = contentIndentDp)
                        .padding(
                            top = if ((fullWidthContent && !forceContentIndent) || !headerSingleLine) 10.dp else 5.dp,
                        ),
                    data = data.toNoteContentUi(),
                    expanded = expanded,
                    textSelectable = textSelectable,
                    nestingCutOffLimit = nestingCutOffLimit,
                    onClick = if (noteCallbacks.onNoteClick != null) {
                        {
                            launchRippleEffect(it)
                            noteCallbacks.onNoteClick.invoke(data.postId)
                        }
                    } else {
                        null
                    },
                    onUrlClick = { localUriHandler.openUriSafely(it) },
                    couldAutoPlay = couldAutoPlay,
                    noteCallbacks = noteCallbacks,
                    onVideoSoundToggle = onVideoSoundToggle,
                    onPollOptionSelected = onPollOptionSelected,
                )

                if (isFeedLayout) {
                    Box(modifier = Modifier.padding(start = contentIndentDp)) {
                        contentFooter()
                    }
                } else {
                    contentFooter()
                }

                if (!showNoteStatCounts) {
                    PrimalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }

                FeedNoteActionsRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (showNoteStatCounts) 12.dp else 16.dp)
                        .padding(start = contentIndentDp)
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

        if (isFeedLayout) {
            UniversalAvatarThumbnail(
                modifier = Modifier.padding(avatarPaddingValues),
                avatarSize = avatarSizeDp,
                avatarCdnImage = data.authorAvatarCdnImage,
                legendaryCustomization = data.authorLegendaryCustomization,
                avatarBlossoms = data.authorBlossoms,
                onClick = if (noteCallbacks.onProfileClick != null) {
                    { noteCallbacks.onProfileClick.invoke(data.authorId) }
                } else {
                    null
                },
                isLive = data.isAuthorLiveStreamingNow,
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
    PrimalPreview(primalTheme = PrimalTheme.Ice) {
        FeedNoteCard(
            data = feedPostUi,
            state = NoteContract.UiState(activeAccountUserId = ""),
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
    PrimalPreview(primalTheme = PrimalTheme.Ice) {
        FeedNoteCard(
            data = feedPostUi,
            state = NoteContract.UiState(activeAccountUserId = ""),
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
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        FeedNoteCard(
            data = feedPostUi,
            state = NoteContract.UiState(activeAccountUserId = ""),
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
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        FeedNoteCard(
            data = feedPostUi,
            state = NoteContract.UiState(activeAccountUserId = ""),
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
    PrimalPreview(primalTheme = PrimalTheme.Ice) {
        FeedNoteCard(
            data = feedPostUi,
            state = NoteContract.UiState(activeAccountUserId = ""),
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
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        FeedNoteCard(
            data = feedPostUi,
            state = NoteContract.UiState(activeAccountUserId = ""),
            eventPublisher = {},
            headerSingleLine = true,
            fullWidthContent = false,
            forceContentIndent = true,
            drawLineBelowAvatar = true,
        )
    }
}

@Suppress("MagicNumber")
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewFeedNoteCardWithSingleChoicePollPending() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        FeedNoteCard(
            data = FeedPostUi(
                postId = "pollPost1",
                content = "What type of people love Nostr the most? #nostr #polloftheday",
                authorId = "npubMiljan",
                authorName = "miljan",
                authorHandle = "miljan@primal.net",
                authorInternetIdentifier = "miljan@primal.net",
                authorAvatarCdnImage = CdnImage(sourceUrl = "https://i.imgur.com/Z8dpmvc.png"),
                timestamp = Instant.now().minus(4, ChronoUnit.MINUTES),
                nostrUris = emptyList(),
                stats = EventStatsUi(
                    repliesCount = 22,
                    likesCount = 56,
                    repostsCount = 14,
                    satsZapped = 2475,
                    zapsCount = 8,
                ),
                hashtags = listOf("#nostr", "#polloftheday"),
                rawNostrEventJson = "",
                poll = PollUi(
                    options = listOf(
                        PollOptionUi(id = "1", label = "\uD83D\uDC40 Conspiracy Contemplators"),
                        PollOptionUi(id = "2", label = "\uD83C\uDF3D Corn Conglomerators"),
                        PollOptionUi(id = "3", label = "\uD83E\uDD65 Coconut Connoiseurs"),
                        PollOptionUi(id = "4", label = "\uD83D\uDC47 All of the above"),
                    ),
                    endsAt = Instant.now().plus(Duration.ofDays(2).plusMinutes(56)),
                    state = PollState.Pending,
                ),
            ),
            state = NoteContract.UiState(activeAccountUserId = ""),
            eventPublisher = {},
            headerSingleLine = true,
            fullWidthContent = false,
        )
    }
}

@Suppress("MagicNumber")
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewFeedNoteCardWithSingleChoicePollVoted() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        FeedNoteCard(
            data = FeedPostUi(
                postId = "pollPost2",
                content = "What type of people love Nostr the most? #nostr #polloftheday",
                authorId = "npubMiljan",
                authorName = "miljan",
                authorHandle = "miljan@primal.net",
                authorInternetIdentifier = "miljan@primal.net",
                authorAvatarCdnImage = CdnImage(sourceUrl = "https://i.imgur.com/Z8dpmvc.png"),
                timestamp = Instant.now().minus(4, ChronoUnit.MINUTES),
                nostrUris = emptyList(),
                stats = EventStatsUi(
                    repliesCount = 22,
                    likesCount = 56,
                    repostsCount = 14,
                    satsZapped = 2475,
                    zapsCount = 8,
                ),
                hashtags = listOf("#nostr", "#polloftheday"),
                rawNostrEventJson = "",
                poll = PollUi(
                    options = listOf(
                        PollOptionUi(
                            id = "1",
                            label = "\uD83D\uDC40 Conspiracy Contemplators",
                            votePercentage = 0f,
                        ),
                        PollOptionUi(
                            id = "2",
                            label = "\uD83C\uDF3D Corn Conglomerators",
                            votePercentage = 0.222f,
                        ),
                        PollOptionUi(
                            id = "3",
                            label = "\uD83E\uDD65 Coconut Connoiseurs",
                            votePercentage = 0.111f,
                        ),
                        PollOptionUi(
                            id = "4",
                            label = "\uD83D\uDC47 All of the above",
                            votePercentage = 0.666f,
                            isWinner = true,
                        ),
                    ),
                    endsAt = Instant.now().plus(Duration.ofDays(2).plusMinutes(56)),
                    state = PollState.Voted,
                    userVotedOptionId = "4",
                ),
            ),
            state = NoteContract.UiState(activeAccountUserId = ""),
            eventPublisher = {},
            headerSingleLine = true,
            fullWidthContent = false,
        )
    }
}

@Suppress("MagicNumber")
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewFeedNoteCardWithSingleChoicePollEnded() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        FeedNoteCard(
            data = FeedPostUi(
                postId = "pollPost3",
                content = "What type of people love Nostr the most? #nostr #polloftheday",
                authorId = "npubMiljan",
                authorName = "miljan",
                authorHandle = "miljan@primal.net",
                authorInternetIdentifier = "miljan@primal.net",
                authorAvatarCdnImage = CdnImage(sourceUrl = "https://i.imgur.com/Z8dpmvc.png"),
                timestamp = Instant.now().minus(4, ChronoUnit.MINUTES),
                nostrUris = emptyList(),
                stats = EventStatsUi(
                    repliesCount = 22,
                    likesCount = 56,
                    repostsCount = 14,
                    satsZapped = 2475,
                    zapsCount = 8,
                ),
                hashtags = listOf("#nostr", "#polloftheday"),
                rawNostrEventJson = "",
                poll = PollUi(
                    options = listOf(
                        PollOptionUi(
                            id = "1",
                            label = "\uD83D\uDC40 Conspiracy Contemplators",
                            votePercentage = 0.035f,
                        ),
                        PollOptionUi(
                            id = "2",
                            label = "\uD83C\uDF3D Corn Conglomerators",
                            votePercentage = 0.243f,
                        ),
                        PollOptionUi(
                            id = "3",
                            label = "\uD83E\uDD65 Coconut Connoiseurs",
                            votePercentage = 0.093f,
                        ),
                        PollOptionUi(
                            id = "4",
                            label = "\uD83D\uDC47 All of the above",
                            votePercentage = 0.629f,
                            isWinner = true,
                        ),
                    ),
                    endsAt = Instant.now().minus(Duration.ofDays(1)),
                    state = PollState.Ended,
                    userVotedOptionId = "4",
                ),
            ),
            state = NoteContract.UiState(activeAccountUserId = ""),
            eventPublisher = {},
            headerSingleLine = true,
            fullWidthContent = false,
        )
    }
}

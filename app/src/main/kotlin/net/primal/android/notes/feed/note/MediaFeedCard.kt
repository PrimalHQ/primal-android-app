package net.primal.android.notes.feed.note

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.time.Instant
import kotlinx.coroutines.delay
import net.primal.android.R
import net.primal.android.core.activity.LocalContentDisplaySettings
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.PrimalAsyncImage
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedBookmark
import net.primal.android.core.compose.icons.primaliconpack.FeedBookmarkFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedNewLike
import net.primal.android.core.compose.icons.primaliconpack.FeedNewLikeFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedNewReply
import net.primal.android.core.compose.icons.primaliconpack.FeedNewReplyFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedNewReposts
import net.primal.android.core.compose.icons.primaliconpack.FeedNewRepostsFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedNewZap
import net.primal.android.core.compose.icons.primaliconpack.FeedNewZapFilled
import net.primal.android.core.compose.zaps.FeedNoteTopZapsSection
import net.primal.android.core.compose.zaps.ZAP_ACTION_DELAY
import net.primal.android.core.errors.UiError
import net.primal.android.core.ext.openUriSafely
import net.primal.android.core.utils.TextMatcher
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostAction
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.asNeventString
import net.primal.android.notes.feed.note.NoteContract.UiEvent
import net.primal.android.notes.feed.note.ui.FeedNoteHeader
import net.primal.android.notes.feed.note.ui.NoteDropdownMenuIcon
import net.primal.android.notes.feed.note.ui.attachment.NoteAttachmentVideoPreview
import net.primal.android.notes.feed.note.ui.attachment.findImageSize
import net.primal.android.notes.feed.note.ui.events.MediaClickEvent
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.theme.AppTheme
import net.primal.core.utils.detectUrls
import net.primal.domain.links.EventUriType
import net.primal.domain.nostr.ReactionType
import net.primal.domain.utils.canZap

private val ActionIconSize = 17.sp
private const val ZapIconSizeMultiplier = 1.2f
private val ActionSpacing = 20.dp
private const val MORE_TEXT_THRESHOLD = 80
private val MediumSpacing = 10.dp
private const val PORTRAIT_THRESHOLD = 1.2f
private val PortraitGradientHeight = 120.dp

private fun isPortraitVideo(eventUri: EventUriUi): Boolean {
    if (eventUri.type != EventUriType.Video) return false
    val w = eventUri.originalWidth ?: eventUri.variants?.firstOrNull()?.width
    val h = eventUri.originalHeight ?: eventUri.variants?.firstOrNull()?.height
    return w != null && h != null && w > 0 && h.toFloat() / w.toFloat() >= PORTRAIT_THRESHOLD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaFeedCard(
    data: FeedPostUi,
    noteCallbacks: NoteCallbacks = NoteCallbacks(),
    couldAutoPlay: Boolean = false,
    onGoToWallet: (() -> Unit)? = null,
    onUiError: ((UiError) -> Unit)? = null,
) {
    val viewModel = hiltViewModel<NoteViewModel, NoteViewModel.Factory>(
        key = "mediaNoteViewModel\$${data.postId}",
        creationCallback = { it.create(noteId = data.postId) },
    )
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(viewModel, uiState.error, onUiError) {
        uiState.error?.let { onUiError?.invoke(it) }
        viewModel.setEvent(UiEvent.DismissError)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect {
            when (it) {
                NoteContract.SideEffect.NoteDeleted -> Unit
            }
        }
    }

    val dialogsState = rememberNoteCardDialogsState()
    NoteCardDialogs(
        dialogsState = dialogsState,
        data = data,
        noteState = uiState,
        eventPublisher = viewModel::setEvent,
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
    )

    var expanded by rememberSaveable { mutableStateOf(false) }

    MediaFeedCardBody(
        data = data,
        state = uiState,
        eventPublisher = viewModel::setEvent,
        noteCallbacks = noteCallbacks,
        dialogsState = dialogsState,
        couldAutoPlay = couldAutoPlay,
        expanded = expanded,
        onExpandClick = { expanded = true },
    )
}

@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
private fun MediaFeedCardBody(
    data: FeedPostUi,
    state: NoteContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    noteCallbacks: NoteCallbacks,
    dialogsState: NoteCardDialogsState,
    couldAutoPlay: Boolean,
    expanded: Boolean,
    onExpandClick: () -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()
    val displaySettings = LocalContentDisplaySettings.current
    val avatarSizeDp = displaySettings.contentAppearance.noteAvatarSize

    val mediaUris = remember(data.uris) {
        data.uris.filter { it.type == EventUriType.Image || it.type == EventUriType.Video }
    }
    val isPortrait = remember(mediaUris) {
        mediaUris.firstOrNull()?.let { isPortraitVideo(it) } == true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                graphicsLayer.record { this@drawWithContent.drawContent() }
                drawLayer(graphicsLayer)
            }
            .background(AppTheme.colorScheme.surfaceVariant)
            .clickable(enabled = noteCallbacks.onNoteClick != null) {
                noteCallbacks.onNoteClick?.invoke(data.postId)
            },
    ) {
        if (!isPortrait) {
            MediaFeedHeader(
                data = data,
                state = state,
                avatarSizeDp = avatarSizeDp,
                graphicsLayer = graphicsLayer,
                eventPublisher = eventPublisher,
                noteCallbacks = noteCallbacks,
                onShowDeleteDialog = { dialogsState.showDeleteDialog = true },
                onShowReportDialog = { dialogsState.showReportDialog = true },
            )
        }

        if (mediaUris.isNotEmpty()) {
            MediaFeedMediaSection(
                data = data,
                state = state,
                mediaUris = mediaUris,
                isPortrait = isPortrait,
                couldAutoPlay = couldAutoPlay,
                avatarSizeDp = avatarSizeDp,
                graphicsLayer = graphicsLayer,
                eventPublisher = eventPublisher,
                noteCallbacks = noteCallbacks,
                dialogsState = dialogsState,
            )
        }

        Spacer(modifier = Modifier.height(MediumSpacing))

        if (data.eventZaps.isNotEmpty()) {
            FeedNoteTopZapsSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                zaps = data.eventZaps,
                onClick = noteCallbacks.onEventReactionsClick?.let {
                    { it.invoke(data.postId, ReactionType.ZAPS, null) }
                },
            )
            Spacer(modifier = Modifier.height(MediumSpacing))
        }

        MediaFeedActionsRow(
            eventStats = data.stats,
            isBookmarked = data.isBookmarked,
            onPostAction = { postAction ->
                when (postAction) {
                    FeedPostAction.Reply ->
                        noteCallbacks.onNoteReplyClick?.invoke(data.asNeventString())

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

                    FeedPostAction.Like -> eventPublisher(
                        UiEvent.PostLikeAction(
                            postId = data.postId,
                            postAuthorId = data.authorId,
                        ),
                    )

                    FeedPostAction.Repost -> dialogsState.showRepostConfirmation = true

                    FeedPostAction.Bookmark ->
                        eventPublisher(UiEvent.BookmarkAction(noteId = data.postId))
                }
            },
            onPostLongPressAction = { postAction ->
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
        )

        Spacer(modifier = Modifier.height(MediumSpacing))

        MediaFeedContentSection(
            authorName = data.authorName,
            content = data.content,
            hashtags = data.hashtags,
            timestamp = data.timestamp,
            expanded = expanded,
            onExpandClick = onExpandClick,
            noteCallbacks = noteCallbacks,
        )
    }
}

@Composable
private fun MediaFeedMediaSection(
    data: FeedPostUi,
    state: NoteContract.UiState,
    mediaUris: List<EventUriUi>,
    isPortrait: Boolean,
    couldAutoPlay: Boolean,
    avatarSizeDp: Dp,
    graphicsLayer: GraphicsLayer,
    eventPublisher: (UiEvent) -> Unit,
    noteCallbacks: NoteCallbacks,
    dialogsState: NoteCardDialogsState,
) {
    if (isPortrait) {
        Box(modifier = Modifier.fillMaxWidth()) {
            MediaFeedPager(
                mediaUris = mediaUris,
                couldAutoPlay = couldAutoPlay,
                onMediaClick = noteCallbacks.onMediaClick,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PortraitGradientHeight)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            MediaFeedHeader(
                data = data,
                state = state,
                avatarSizeDp = avatarSizeDp,
                graphicsLayer = graphicsLayer,
                eventPublisher = eventPublisher,
                noteCallbacks = noteCallbacks,
                onShowDeleteDialog = { dialogsState.showDeleteDialog = true },
                onShowReportDialog = { dialogsState.showReportDialog = true },
            )
        }
    } else {
        MediaFeedPager(
            mediaUris = mediaUris,
            couldAutoPlay = couldAutoPlay,
            onMediaClick = noteCallbacks.onMediaClick,
        )
    }
}

@Composable
private fun MediaFeedHeader(
    data: FeedPostUi,
    state: NoteContract.UiState,
    avatarSizeDp: Dp,
    graphicsLayer: GraphicsLayer,
    eventPublisher: (UiEvent) -> Unit,
    noteCallbacks: NoteCallbacks,
    onShowDeleteDialog: () -> Unit,
    onShowReportDialog: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        FeedNoteHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 44.dp, top = 10.dp, bottom = 10.dp),
            authorDisplayName = data.authorName,
            authorAvatarVisible = true,
            authorAvatarSize = avatarSizeDp,
            authorAvatarCdnImage = data.authorAvatarCdnImage,
            authorInternetIdentifier = data.authorInternetIdentifier,
            authorLegendaryCustomization = data.authorLegendaryCustomization,
            authorBlossoms = data.authorBlossoms,
            authorId = data.authorId,
            isLive = data.isAuthorLiveStreamingNow,
            onAuthorAvatarClick = noteCallbacks.onProfileClick?.let {
                { it(data.authorId) }
            },
        )

        NoteDropdownMenuIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp)
                .size(40.dp)
                .padding(top = 18.dp)
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
            onRequestDeleteClick = onShowDeleteDialog,
            onReportContentClick = onShowReportDialog,
        )
    }
}

@Composable
private fun MediaFeedPager(
    mediaUris: List<EventUriUi>,
    couldAutoPlay: Boolean,
    onMediaClick: ((MediaClickEvent) -> Unit)?,
) {
    val pagerState = rememberPagerState(pageCount = { mediaUris.size })

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val pagerHeight = findImageSize(eventUri = mediaUris.first()).height

        Column {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pagerHeight),
            ) { page ->
                val media = mediaUris[page]
                if (media.type == EventUriType.Video) {
                    NoteAttachmentVideoPreview(
                        modifier = Modifier.fillMaxSize(),
                        eventUri = media,
                        onVideoClick = { positionMs ->
                            onMediaClick?.invoke(
                                MediaClickEvent(
                                    noteId = media.eventId,
                                    eventUriType = media.type,
                                    mediaUrl = media.url,
                                    positionMs = positionMs,
                                ),
                            )
                        },
                        allowAutoPlay = true,
                        couldAutoPlay = couldAutoPlay && pagerState.currentPage == page,
                    )
                } else {
                    PrimalAsyncImage(
                        model = media.url,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (onMediaClick != null) {
                                    Modifier.clickable {
                                        onMediaClick(
                                            MediaClickEvent(
                                                noteId = media.eventId,
                                                eventUriType = media.type,
                                                mediaUrl = media.url,
                                                positionMs = 0L,
                                            ),
                                        )
                                    }
                                } else {
                                    Modifier
                                },
                            ),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                    )
                }
            }

            if (mediaUris.size > 1) {
                PageIndicatorDots(
                    pageCount = mediaUris.size,
                    currentPage = pagerState.currentPage,
                )
            }
        }
    }
}

@Composable
private fun PageIndicatorDots(pageCount: Int, currentPage: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = MediumSpacing),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(pageCount) { index ->
            val isSelected = currentPage == index
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (isSelected) 7.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            AppTheme.colorScheme.primary
                        } else {
                            AppTheme.extraColorScheme.onSurfaceVariantAlt2
                                .copy(alpha = 0.4f)
                        },
                    ),
            )
        }
    }
}

@Composable
private fun MediaFeedContentSection(
    authorName: String,
    content: String,
    hashtags: List<String>,
    timestamp: Instant?,
    expanded: Boolean,
    onExpandClick: () -> Unit,
    noteCallbacks: NoteCallbacks,
) {
    val localUriHandler = LocalUriHandler.current
    val highlightColor = AppTheme.colorScheme.secondary
    val displaySettings = LocalContentDisplaySettings.current
    val contentTextStyle = AppTheme.typography.bodyMedium.copy(
        fontSize = displaySettings.contentAppearance.noteUsernameSize,
        lineHeight = displaySettings.contentAppearance.noteUsernameSize,
        color = AppTheme.colorScheme.onSurface,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 16.dp),
    ) {
        // Remove URL filtering when kind 20 events are supported.
        val displayContent = content.lines()
            .filterNot { it.startsWith("http://") || it.startsWith("https://") }
            .joinToString(" ")
            .trim()

        val annotatedText = remember(authorName, displayContent, hashtags, highlightColor) {
            buildMediaFeedAnnotatedString(
                authorName = authorName,
                displayContent = displayContent,
                hashtags = hashtags,
                highlightColor = highlightColor,
            )
        }

        val maxLines = if (expanded) Int.MAX_VALUE else 2
        PrimalClickableText(
            text = annotatedText,
            style = contentTextStyle,
            maxLines = maxLines,
            overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
            onClick = { position, _ ->
                val annotation = annotatedText.getStringAnnotations(position, position).firstOrNull()
                when (annotation?.tag) {
                    HASHTAG_ANNOTATION_TAG -> noteCallbacks.onHashtagClick?.invoke(annotation.item)
                    URL_ANNOTATION_TAG -> localUriHandler.openUriSafely(annotation.item)
                    else -> if (!expanded && displayContent.isNotEmpty()) onExpandClick()
                }
            },
        )

        if (!expanded && displayContent.length > MORE_TEXT_THRESHOLD) {
            Text(
                text = stringResource(id = R.string.feed_more),
                modifier = Modifier.clickable { onExpandClick() },
                style = contentTextStyle,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }

        if (timestamp != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = timestamp.asBeforeNowFormat(shortFormat = false),
                style = contentTextStyle,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }
    }
}

private const val HASHTAG_ANNOTATION_TAG = "hashtag"
private const val URL_ANNOTATION_TAG = "url"

private fun buildMediaFeedAnnotatedString(
    authorName: String,
    displayContent: String,
    hashtags: List<String>,
    highlightColor: Color,
): AnnotatedString =
    buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(authorName)
        }

        if (displayContent.isNotEmpty()) {
            append(" ")
            val contentOffset = length

            append(displayContent)

            TextMatcher(content = displayContent, texts = hashtags, repeatingOccurrences = true)
                .matches()
                .forEach { match ->
                    addStyle(
                        style = SpanStyle(color = highlightColor),
                        start = contentOffset + match.startIndex,
                        end = contentOffset + match.endIndex,
                    )
                    addStringAnnotation(
                        tag = HASHTAG_ANNOTATION_TAG,
                        annotation = match.value,
                        start = contentOffset + match.startIndex,
                        end = contentOffset + match.endIndex,
                    )
                }

            TextMatcher(content = displayContent, texts = displayContent.detectUrls(), repeatingOccurrences = true)
                .matches()
                .forEach { match ->
                    addStyle(
                        style = SpanStyle(color = highlightColor),
                        start = contentOffset + match.startIndex,
                        end = contentOffset + match.endIndex,
                    )
                    addStringAnnotation(
                        tag = URL_ANNOTATION_TAG,
                        annotation = match.value,
                        start = contentOffset + match.startIndex,
                        end = contentOffset + match.endIndex,
                    )
                }
        }
    }

@Suppress("LongMethod")
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaFeedActionsRow(
    eventStats: EventStatsUi,
    isBookmarked: Boolean,
    onPostAction: (FeedPostAction) -> Unit,
    onPostLongPressAction: (FeedPostAction) -> Unit,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    var isZapCooldownActive by remember { mutableStateOf(false) }
    LaunchedEffect(isZapCooldownActive) {
        if (isZapCooldownActive) {
            delay(ZAP_ACTION_DELAY)
            isZapCooldownActive = false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(ActionSpacing),
        ) {
            ActionStat(
                action = FeedPostAction.Reply,
                count = eventStats.repliesCount,
                highlighted = eventStats.userReplied,
                icon = PrimalIcons.FeedNewReply,
                iconHighlighted = PrimalIcons.FeedNewReplyFilled,
                highlightColor = AppTheme.extraColorScheme.replied,
                contentDescription = stringResource(R.string.accessibility_replies_count),
                numberFormat = numberFormat,
                onPostAction = onPostAction,
                onPostLongPressAction = onPostLongPressAction,
            )
            ActionStat(
                action = FeedPostAction.Zap,
                count = eventStats.satsZapped,
                highlighted = eventStats.userZapped,
                icon = PrimalIcons.FeedNewZap,
                iconHighlighted = PrimalIcons.FeedNewZapFilled,
                highlightColor = AppTheme.extraColorScheme.zapped,
                contentDescription = stringResource(R.string.accessibility_zaps_count),
                iconSize = ActionIconSize.times(ZapIconSizeMultiplier),
                numberFormat = numberFormat,
                onPostAction = { action ->
                    if (!isZapCooldownActive) {
                        isZapCooldownActive = true
                        onPostAction(action)
                    }
                },
                onPostLongPressAction = onPostLongPressAction,
            )
            ActionStat(
                action = FeedPostAction.Like,
                count = eventStats.likesCount,
                highlighted = eventStats.userLiked,
                icon = PrimalIcons.FeedNewLike,
                iconHighlighted = PrimalIcons.FeedNewLikeFilled,
                highlightColor = AppTheme.extraColorScheme.liked,
                contentDescription = stringResource(R.string.accessibility_likes_count),
                numberFormat = numberFormat,
                onPostAction = onPostAction,
                onPostLongPressAction = onPostLongPressAction,
            )
            ActionStat(
                action = FeedPostAction.Repost,
                count = eventStats.repostsCount,
                highlighted = eventStats.userReposted,
                icon = PrimalIcons.FeedNewReposts,
                iconHighlighted = PrimalIcons.FeedNewRepostsFilled,
                highlightColor = AppTheme.extraColorScheme.reposted,
                contentDescription = stringResource(R.string.accessibility_repost_count),
                numberFormat = numberFormat,
                onPostAction = onPostAction,
                onPostLongPressAction = onPostLongPressAction,
            )
        }

        ActionStat(
            action = FeedPostAction.Bookmark,
            count = 0,
            highlighted = isBookmarked,
            icon = PrimalIcons.FeedBookmark,
            iconHighlighted = PrimalIcons.FeedBookmarkFilled,
            highlightColor = AppTheme.extraColorScheme.bookmarked,
            contentDescription = stringResource(R.string.accessibility_bookmark),
            numberFormat = numberFormat,
            onPostAction = onPostAction,
            onPostLongPressAction = onPostLongPressAction,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActionStat(
    action: FeedPostAction,
    count: Long,
    highlighted: Boolean,
    icon: ImageVector,
    iconHighlighted: ImageVector,
    highlightColor: Color,
    contentDescription: String,
    numberFormat: NumberFormat,
    iconSize: TextUnit = ActionIconSize,
    onPostAction: (FeedPostAction) -> Unit,
    onPostLongPressAction: (FeedPostAction) -> Unit,
) {
    val defaultColor = AppTheme.extraColorScheme.onSurfaceVariantAlt4
    IconText(
        modifier = Modifier
            .animateContentSize()
            .combinedClickable(
                enabled = action != FeedPostAction.Like || !highlighted,
                onClick = { onPostAction(action) },
                onLongClick = { onPostLongPressAction(action) },
            ),
        leadingIcon = if (highlighted) iconHighlighted else icon,
        leadingIconTintColor = if (highlighted) null else defaultColor,
        leadingIconContentDescription = contentDescription,
        iconSize = iconSize,
        text = if (count > 0) numberFormat.format(count) else "",
        color = if (highlighted) highlightColor else defaultColor,
        style = AppTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

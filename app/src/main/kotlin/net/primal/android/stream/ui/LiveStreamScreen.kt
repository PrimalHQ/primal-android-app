@file:OptIn(ExperimentalSharedTransitionApi::class)

package net.primal.android.stream.ui

import android.content.Context
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateBounds
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.session.MediaController
import java.text.NumberFormat
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.HeightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalSeekBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.bubble.AnchorHandle
import net.primal.android.core.compose.bubble.AnchoredBubble
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.NavWalletBoltFilled
import net.primal.android.core.compose.profile.approvals.FollowsApprovalAlertDialog
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.rememberFullScreenController
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.core.ext.onDragDownBeyond
import net.primal.android.core.ext.openUriSafely
import net.primal.android.core.pip.rememberIsInPipMode
import net.primal.android.core.video.toggle
import net.primal.android.editor.ui.NoteOutlinedTextField
import net.primal.android.editor.ui.NoteTagUserLazyColumn
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.notes.feed.model.NoteNostrUriUi
import net.primal.android.notes.feed.zaps.ZapHost
import net.primal.android.notes.feed.zaps.ZapHostState
import net.primal.android.notes.feed.zaps.rememberZapHostState
import net.primal.android.stream.LiveStreamContract
import net.primal.android.stream.player.LIVE_EDGE_THRESHOLD_MS
import net.primal.android.stream.player.PLAYER_STATE_UPDATE_INTERVAL
import net.primal.android.stream.player.SEEK_BACK_MS
import net.primal.android.stream.player.SEEK_FORWARD_MS
import net.primal.android.stream.player.SHARED_TRANSITION_LOADING_PLAYER_KEY
import net.primal.android.stream.player.SHARED_TRANSITION_PLAYER_KEY
import net.primal.android.stream.player.VIDEO_ASPECT_RATIO_HEIGHT
import net.primal.android.stream.player.VIDEO_ASPECT_RATIO_WIDTH
import net.primal.android.theme.AppTheme
import net.primal.core.utils.detectUrls
import net.primal.domain.links.EventUriNostrType
import net.primal.domain.links.ReferencedUser
import net.primal.domain.nostr.utils.clearAtSignFromNostrUris
import net.primal.domain.nostr.utils.parseNostrUris
import net.primal.domain.streams.StreamContentModerationMode
import net.primal.domain.streams.StreamStatus
import net.primal.domain.utils.isLightningAddress
import net.primal.domain.wallet.DraftTx

private const val URL_ANNOTATION_TAG = "url"
private const val COLLAPSED_MODE_CHAT_ITEMS_THRESHOLD = 20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamScreen(
    state: LiveStreamContract.UiState,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    mediaController: MediaController,
    callbacks: LiveStreamContract.ScreenCallbacks,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onRetry: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.playerState.isPlaying) {
        while (state.playerState.isPlaying) {
            eventPublisher(
                LiveStreamContract.UiEvent.OnPlayerStateUpdate(
                    currentTime = mediaController.currentPosition,
                    bufferedPosition = mediaController.bufferedPosition,
                    atLiveEdge = isPlaybackAtLiveEdge(mediaController),
                    isPlaying = mediaController.isPlaying,
                ),
            )
            delay(PLAYER_STATE_UPDATE_INTERVAL)
        }
    }

    val receiverName = when (val sheet = state.activeBottomSheet) {
        is ActiveBottomSheet.ZapDetails -> sheet.zap.zapperName
        is ActiveBottomSheet.ChatDetails -> sheet.message.authorProfile.authorDisplayName
        else -> state.streamInfo?.mainHostProfile?.authorDisplayName
    }

    val zapHostState = rememberZapHostState(zappingState = state.zappingState, receiverName = receiverName)

    ZapHost(
        zapHostState = zapHostState,
        onZap = { zapAmount, zapDescription ->
            eventPublisher(
                LiveStreamContract.UiEvent.ZapStream(
                    zapAmount = zapAmount.toULong(),
                    zapDescription = zapDescription,
                ),
            )
        },
        onGoToWallet = callbacks.onGoToWallet,
    )

    if (state.shouldApproveProfileAction != null) {
        FollowsApprovalAlertDialog(
            followsApproval = state.shouldApproveProfileAction,
            onFollowsActionsApproved = {
                eventPublisher(LiveStreamContract.UiEvent.ApproveFollowsActions(it.actions))
            },
            onClose = { eventPublisher(LiveStreamContract.UiEvent.DismissConfirmFollowUnfollowAlertDialog) },
        )
    }

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = { eventPublisher(LiveStreamContract.UiEvent.DismissError) },
    )

    LiveStreamScaffold(
        state = state,
        mediaController = mediaController,
        eventPublisher = eventPublisher,
        callbacks = callbacks,
        snackbarHostState = snackbarHostState,
        zapHostState = zapHostState,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        onRetry = onRetry,
    )
}

@Composable
private fun LiveStreamScaffold(
    state: LiveStreamContract.UiState,
    mediaController: MediaController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    callbacks: LiveStreamContract.ScreenCallbacks,
    snackbarHostState: SnackbarHostState,
    zapHostState: ZapHostState,
    onRetry: () -> Unit,
) {
    if (state.activeBottomSheet != ActiveBottomSheet.None) {
        BackHandler {
            eventPublisher(LiveStreamContract.UiEvent.ChangeActiveBottomSheet(ActiveBottomSheet.None))
        }
    }

    var controlsVisible by remember { mutableStateOf(false) }
    var menuVisible by remember { mutableStateOf(false) }

    LaunchedEffect(controlsVisible, menuVisible) {
        if (controlsVisible && !menuVisible) {
            delay(5.seconds)
            controlsVisible = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            BoxWithConstraints {
                val playerHeight = this.maxWidth * (VIDEO_ASPECT_RATIO_HEIGHT / VIDEO_ASPECT_RATIO_WIDTH)
                val topInset = paddingValues.calculateTopPadding()
                val bottomInset = paddingValues.calculateBottomPadding()

                val bottomSheetHeight = remember(this.maxHeight, playerHeight, topInset, bottomInset) {
                    (this.maxHeight - topInset - playerHeight - bottomInset).coerceAtLeast(0.dp)
                }

                LiveStreamContent(
                    state = state,
                    mediaController = mediaController,
                    eventPublisher = eventPublisher,
                    paddingValues = paddingValues,
                    callbacks = callbacks,
                    controlsVisible = controlsVisible,
                    menuVisible = menuVisible,
                    playerHeight = playerHeight,
                    onControlsVisibilityChange = { controlsVisible = !controlsVisible },
                    onMenuVisibilityChange = { menuVisible = it },
                    onZapClick = { zapHostState.showZapOptionsOrShowWarning() },
                    onChatSettingsClick = {
                        eventPublisher(
                            LiveStreamContract.UiEvent.ChangeActiveBottomSheet(ActiveBottomSheet.StreamSettings),
                        )
                    },
                    onInfoClick = {
                        eventPublisher(LiveStreamContract.UiEvent.ChangeActiveBottomSheet(ActiveBottomSheet.StreamInfo))
                    },
                    onChatMessageClick = {
                        eventPublisher(
                            LiveStreamContract.UiEvent.ChangeActiveBottomSheet(ActiveBottomSheet.ChatDetails(it)),
                        )
                    },
                    onZapMessageClick = {
                        eventPublisher(
                            LiveStreamContract.UiEvent.ChangeActiveBottomSheet(ActiveBottomSheet.ZapDetails(it)),
                        )
                    },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onRetry = onRetry,
                )

                LiveStreamBottomSheet(
                    state = state,
                    bottomSheetHeight = bottomSheetHeight,
                    callbacks = callbacks,
                    snackbarHostState = snackbarHostState,
                    eventPublisher = eventPublisher,
                )
            }
        },
    )
}

@Composable
private fun LiveStreamBottomSheet(
    state: LiveStreamContract.UiState,
    bottomSheetHeight: Dp,
    callbacks: LiveStreamContract.ScreenCallbacks,
    snackbarHostState: SnackbarHostState,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LiveStreamModalBottomSheetHost(
        activeSheet = state.activeBottomSheet,
        streamInfo = state.streamInfo,
        zaps = state.zaps,
        isStreamLive = state.streamInfo?.streamStatus == StreamStatus.LIVE,
        activeUserId = state.activeUserId,
        mainHostStreamsMuted = state.mainHostStreamsMuted,
        contentModeration = state.contentModerationMode,
        followerCountMap = state.profileIdToFollowerCount,
        liveProfiles = state.liveProfiles,
        mutedProfiles = state.activeUserMutedProfiles,
        followedProfiles = state.activeUserFollowedProfiles,
        bottomSheetHeight = bottomSheetHeight,
        onDismiss = { eventPublisher(LiveStreamContract.UiEvent.ChangeActiveBottomSheet(ActiveBottomSheet.None)) },
        onFollow = { eventPublisher(LiveStreamContract.UiEvent.FollowAction(it)) },
        onUnfollow = { eventPublisher(LiveStreamContract.UiEvent.UnfollowAction(it)) },
        onMute = { eventPublisher(LiveStreamContract.UiEvent.MuteAction(it)) },
        onUnmute = { eventPublisher(LiveStreamContract.UiEvent.UnmuteAction(it)) },
        onStreamNotificationsChanged = { eventPublisher(LiveStreamContract.UiEvent.ChangeStreamMuted(it)) },
        onContentModerationChanged = { contentModeration ->
            when (contentModeration) {
                StreamContentModerationMode.Moderated ->
                    eventPublisher(LiveStreamContract.UiEvent.ChangeContentModeration(StreamContentModerationMode.None))

                StreamContentModerationMode.None ->
                    eventPublisher(
                        LiveStreamContract.UiEvent.ChangeContentModeration(StreamContentModerationMode.Moderated),
                    )
            }
        },
        onZapClick = { profileDetails ->
            handleZapProfile(
                profileDetails = profileDetails,
                callbacks = callbacks,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState,
                context = context,
            )
        },
        onReport = { reportType, messageId, authorId ->
            eventPublisher(
                LiveStreamContract.UiEvent.ReportMessage(
                    reportType = reportType,
                    messageId = messageId,
                    authorId = authorId,
                ),
            )
        },
        onHashtagClick = callbacks.onHashtagClick,
        onMessageClick = callbacks.onMessageClick,
        onEditProfileClick = callbacks.onEditProfileClick,
        onDrawerQrCodeClick = callbacks.onDrawerQrCodeClick,
        onZapMessageClick = {
            eventPublisher(LiveStreamContract.UiEvent.ChangeActiveBottomSheet(ActiveBottomSheet.ZapDetails(it)))
        },
        onProfileClick = { profileId ->
            eventPublisher(LiveStreamContract.UiEvent.ChangeActiveBottomSheet(ActiveBottomSheet.None))
            callbacks.onProfileClick(profileId)
        },
    )
}

@Composable
private fun StreamPlayer(
    state: LiveStreamContract.UiState,
    isCollapsed: Boolean,
    streamInfo: LiveStreamContract.StreamInfoUi,
    mediaController: MediaController,
    controlsVisible: Boolean,
    menuVisible: Boolean,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    onClose: () -> Unit,
    onQuoteStreamClick: (String) -> Unit,
    onControlsVisibilityChange: () -> Unit,
    onMenuVisibilityChange: (Boolean) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    lookaheadScope: LookaheadScope,
    onRetry: () -> Unit,
) {
    val fullScreenController = rememberFullScreenController()

    with(sharedTransitionScope) {
        ExpandedLiveStreamPlayer(
            modifier = Modifier
                .run {
                    if (isCollapsed) {
                        this
                            .fillMaxHeight()
                            .aspectRatio(VIDEO_ASPECT_RATIO_WIDTH / VIDEO_ASPECT_RATIO_HEIGHT)
                            .clip(AppTheme.shapes.extraSmall)
                    } else {
                        this.skipToLookaheadSize()
                    }
                },

            playerModifier = Modifier
                .animateBounds(lookaheadScope = lookaheadScope)
                .sharedElement(
                    sharedContentState = rememberSharedContentState(key = SHARED_TRANSITION_PLAYER_KEY),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            loadingModifier = Modifier
                .animateBounds(lookaheadScope = lookaheadScope)
                .sharedElement(
                    sharedContentState = rememberSharedContentState(key = SHARED_TRANSITION_LOADING_PLAYER_KEY),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            controlsModifier = Modifier.animateBounds(lookaheadScope = lookaheadScope),
            state = state,
            mediaController = mediaController,
            streamUrl = streamInfo.streamUrl,
            controlsVisible = controlsVisible,
            menuVisible = menuVisible,
            isCollapsed = isCollapsed,
            onPlayPauseClick = { mediaController.toggle() },
            onRewind = {
                eventPublisher(LiveStreamContract.UiEvent.OnSeekStarted)
                val newPosition = (mediaController.currentPosition - SEEK_BACK_MS).coerceAtLeast(0L)
                mediaController.seekTo(newPosition)
                eventPublisher(LiveStreamContract.UiEvent.OnSeek(newPosition))
            },
            onForward = {
                val duration = mediaController.duration
                if (duration != C.TIME_UNSET) {
                    val currentPosition = mediaController.currentPosition
                    val newPosition = currentPosition + SEEK_FORWARD_MS

                    if (newPosition >= duration) {
                        mediaController.seekToDefaultPosition()
                        eventPublisher(LiveStreamContract.UiEvent.OnSeek(duration))
                    } else {
                        eventPublisher(LiveStreamContract.UiEvent.OnSeekStarted)
                        mediaController.seekTo(newPosition)
                        eventPublisher(LiveStreamContract.UiEvent.OnSeek(newPosition))
                    }
                } else {
                    mediaController.seekToDefaultPosition()
                }
            },
            onSoundClick = { eventPublisher(LiveStreamContract.UiEvent.ToggleMute) },
            onClose = onClose,
            onControlsVisibilityChange = onControlsVisibilityChange,
            onMenuVisibilityChange = onMenuVisibilityChange,
            onQuoteClick = onQuoteStreamClick,
            onMuteUserClick = {
                state.streamInfo?.mainHostId?.let { eventPublisher(LiveStreamContract.UiEvent.MuteAction(it)) }
            },
            onUnmuteUserClick = {
                state.streamInfo?.mainHostId?.let { eventPublisher(LiveStreamContract.UiEvent.UnmuteAction(it)) }
            },
            onReportContentClick = { reportType ->
                eventPublisher(LiveStreamContract.UiEvent.ReportAbuse(reportType))
            },
            onRequestDeleteClick = { eventPublisher(LiveStreamContract.UiEvent.RequestDeleteStream) },
            onToggleFullScreenClick = { fullScreenController.toggle() },
            onRetry = onRetry,
        )
    }
}

@Composable
private fun LiveStreamContent(
    state: LiveStreamContract.UiState,
    mediaController: MediaController,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    paddingValues: PaddingValues,
    callbacks: LiveStreamContract.ScreenCallbacks,
    controlsVisible: Boolean,
    menuVisible: Boolean,
    playerHeight: Dp,
    onControlsVisibilityChange: () -> Unit,
    onMenuVisibilityChange: (Boolean) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onZapClick: () -> Unit,
    onInfoClick: () -> Unit,
    onChatSettingsClick: () -> Unit,
    onChatMessageClick: (ChatMessageUi) -> Unit,
    onZapMessageClick: (EventZapUiModel) -> Unit,
    onRetry: () -> Unit,
) {
    val isInPipMode = rememberIsInPipMode()
    val localConfiguration = LocalConfiguration.current
    val isLandscape = localConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (state.streamInfoLoading) {
        PrimalLoadingSpinner()
    }
    val chatListState = rememberLazyListState()
    val isKeyboardVisible by keyboardVisibilityAsState()
    val isCollapsed by remember(
        chatListState.firstVisibleItemIndex,
        localConfiguration.orientation,
        isKeyboardVisible,
    ) {
        mutableStateOf(
            (
                chatListState.firstVisibleItemIndex != 0 &&
                    !isLandscape &&
                    state.chatItems.size > COLLAPSED_MODE_CHAT_ITEMS_THRESHOLD
                ) ||
                isKeyboardVisible,
        )
    }

    val streamInfo = state.streamInfo
    if (streamInfo != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isCollapsed) {
                        AppTheme.extraColorScheme.surfaceVariantAlt3
                    } else {
                        Color.Transparent
                    },
                )
                .padding(top = paddingValues.calculateTopPadding())
                .background(AppTheme.colorScheme.surfaceVariant)
                .padding(horizontal = paddingValues.calculateStartPadding(layoutDirection = LayoutDirection.Ltr))
                .padding(bottom = paddingValues.calculateBottomPadding()),
        ) {
            val boundsTransform = remember { BoundsTransform { _, _ -> tween() } }

            LookaheadScope {
                Column(modifier = Modifier.fillMaxSize()) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .animateBounds(
                                lookaheadScope = this@LookaheadScope,
                                boundsTransform = boundsTransform,
                            )
                            .run {
                                if (isCollapsed) {
                                    this
                                        .onDragDownBeyond(
                                            threshold = 75.dp,
                                            onTriggered = callbacks.onClose,
                                        )
                                        .background(AppTheme.extraColorScheme.surfaceVariantAlt3)
                                        .padding(8.dp)
                                        .padding(end = 8.dp)
                                        .height(96.dp)
                                        .fillMaxWidth()
                                } else {
                                    this
                                }
                            },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        val maxWidth = this.maxWidth
                        val playerWidth = this.maxHeight * VIDEO_ASPECT_RATIO_WIDTH / VIDEO_ASPECT_RATIO_HEIGHT
                        val playerBoxWidth = playerWidth + 12.dp
                        val streamInfoColumnWidth = maxWidth - playerBoxWidth - 6.dp
                        StreamPlayer(
                            state = state,
                            isCollapsed = isCollapsed,
                            streamInfo = streamInfo,
                            mediaController = mediaController,
                            controlsVisible = controlsVisible,
                            menuVisible = menuVisible,
                            eventPublisher = eventPublisher,
                            onClose = callbacks.onClose,
                            onQuoteStreamClick = callbacks.onQuoteStreamClick,
                            onControlsVisibilityChange = onControlsVisibilityChange,
                            onMenuVisibilityChange = onMenuVisibilityChange,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            lookaheadScope = this@LookaheadScope,
                            onRetry = onRetry,
                        )

                        CollapsedStreamInfoColumn(
                            modifier = Modifier
                                .padding(start = playerBoxWidth)
                                .width(streamInfoColumnWidth),
                            isCollapsed = isCollapsed,
                            streamTitle = state.streamInfo.title,
                            viewers = state.streamInfo.viewers,
                            isLive = state.streamInfo.streamStatus == StreamStatus.LIVE,
                        )
                    }
                    if (localConfiguration.orientation != Configuration.ORIENTATION_LANDSCAPE && !isInPipMode) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                        ) {
                            AnimatedVisibility(visible = !isCollapsed) {
                                StreamInfoDisplay(
                                    state = state,
                                    onZapClick = onZapClick,
                                    onInfoClick = onInfoClick,
                                    onChatSettingsClick = onChatSettingsClick,
                                    onDismissStreamControlPopup = {
                                        eventPublisher(LiveStreamContract.UiEvent.DismissStreamControlPopup)
                                    },
                                    onTopZapsClick = {
                                        eventPublisher(
                                            LiveStreamContract.UiEvent.ChangeActiveBottomSheet(
                                                ActiveBottomSheet.StreamZapLeaderboard,
                                            ),
                                        )
                                    },
                                )
                            }

                            LiveChatContent(
                                state = state,
                                listState = chatListState,
                                eventPublisher = eventPublisher,
                                onProfileClick = callbacks.onProfileClick,
                                onNostrUriClick = callbacks.onNostrUriClick,
                                onChatMessageClick = onChatMessageClick,
                                onZapMessageClick = onZapMessageClick,
                            )
                        }
                    }
                }

                val seekBarModifier = if (isLandscape) {
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 20.dp, start = 32.dp, end = 32.dp)
                } else {
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(top = playerHeight - 16.dp)
                }

                AnimatedVisibility(
                    modifier = seekBarModifier,
                    visible = !isInPipMode && !isCollapsed,
                    enter = fadeIn(animationSpec = tween(delayMillis = 250)),
                ) {
                    StreamSeekBar(
                        isVisible = controlsVisible,
                        state = state,
                        mediaController = mediaController,
                        eventPublisher = eventPublisher,
                    )
                }
            }
        }
    }
}

@Composable
private fun CollapsedStreamInfoColumn(
    modifier: Modifier = Modifier,
    isCollapsed: Boolean,
    streamTitle: String,
    isLive: Boolean,
    viewers: Int,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isCollapsed,
        enter = slideInVertically(
            animationSpec = tween(),
            initialOffsetY = { it / 2 },
        ) + fadeIn(animationSpec = tween()),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = streamTitle,
                style = AppTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                ),
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            StreamMetaData(
                isLive = isLive,
                viewers = viewers,
                startedAt = null,
            )
        }
    }
}

@Composable
private fun StreamSeekBar(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    state: LiveStreamContract.UiState,
    mediaController: MediaController,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible && !state.isStreamUnavailable,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val playerState = state.playerState
        val isInteractive = playerState.totalDuration > 0 && !playerState.isLive
        val totalDuration = playerState.totalDuration.takeIf { it > 0L } ?: 1L

        var scrubPositionMs by remember { mutableStateOf<Long?>(null) }
        var wasPlayingBeforeScrub by remember { mutableStateOf(false) }

        val progress = if (playerState.isLive && playerState.atLiveEdge) {
            1f
        } else {
            (playerState.currentTime.toFloat() / totalDuration).coerceIn(0f, 1f)
        }

        val bufferedProgress = (playerState.bufferedPosition.toFloat() / totalDuration).coerceIn(0f, 1f)

        PrimalSeekBar(
            progress = progress,
            bufferedProgress = if (playerState.isLive) 0f else bufferedProgress,
            isInteractive = isInteractive,
            onScrub = { newProgress ->
                if (isInteractive) {
                    if (scrubPositionMs == null) {
                        wasPlayingBeforeScrub = mediaController.isPlaying
                        mediaController.pause()
                        eventPublisher(LiveStreamContract.UiEvent.OnSeekStarted)
                    }
                    scrubPositionMs = (newProgress * totalDuration).toLong()
                }
            },
            onScrubEnd = {
                scrubPositionMs?.let { finalPosition ->
                    mediaController.seekTo(finalPosition)
                    eventPublisher(LiveStreamContract.UiEvent.OnSeek(finalPosition))
                    if (wasPlayingBeforeScrub) {
                        mediaController.play()
                    }
                    scrubPositionMs = null
                    wasPlayingBeforeScrub = false
                }
            },
            totalDurationMs = playerState.totalDuration,
            currentTimeMs = scrubPositionMs ?: playerState.currentTime,
        )
    }
}

@Composable
private fun StreamInfoDisplay(
    state: LiveStreamContract.UiState,
    onDismissStreamControlPopup: () -> Unit,
    onZapClick: () -> Unit,
    onChatSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onTopZapsClick: () -> Unit,
) {
    val streamInfo = state.streamInfo ?: return
    val bottomBorderColor = AppTheme.extraColorScheme.surfaceVariantAlt1
    val anchor = remember { AnchorHandle() }

    Box(
        contentAlignment = Alignment.TopEnd,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .background(
                    AppTheme.extraColorScheme.surfaceVariantAlt2,
                )
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val y = size.height - strokeWidth / 2f
                    drawLine(
                        color = bottomBorderColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth,
                    )
                }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StreamInfoSection(
                title = streamInfo.title,
                viewers = streamInfo.viewers,
                startedAt = streamInfo.startedAt,
                isLive = state.playerState.isLive,
                onInfoClick = onInfoClick,
                onChatSettingsClick = onChatSettingsClick,
                streamControlAnchorHandle = anchor,
            )

            StreamTopZapsSection(
                modifier = Modifier.fillMaxWidth(),
                chatLoading = state.chatLoading,
                topZaps = state.zaps,
                onZapClick = onZapClick,
                onTopZapsClick = onTopZapsClick,
            )
        }

        AnchoredBubble(
            anchor = anchor,
            text = stringResource(id = R.string.live_stream_stream_control_popup_text),
            visible = state.showStreamControlPopup,
            onDismiss = onDismissStreamControlPopup,
        )
    }
}

@Composable
private fun LiveChatListOrSearch(
    modifier: Modifier = Modifier,
    state: LiveStreamContract.UiState,
    listState: LazyListState,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    onProfileClick: (String) -> Unit,
    onNostrUriClick: (String) -> Unit,
    onChatMessageClick: (ChatMessageUi) -> Unit,
    onZapMessageClick: (EventZapUiModel) -> Unit,
) {
    if (!state.userTaggingState.isUserTaggingActive) {
        if (state.chatLoading) {
            HeightAdjustableLoadingLazyListPlaceholder(height = 140.dp)
        } else if (state.chatItems.isEmpty()) {
            LiveChatEmpty(
                modifier = modifier,
            )
        } else {
            LazyColumn(
                modifier = modifier,
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                reverseLayout = true,
            ) {
                items(
                    items = state.chatItems,
                    key = { it.uniqueId },
                    contentType = {
                        when (it) {
                            is StreamChatItem.ChatMessageItem -> "chatMessage"
                            is StreamChatItem.ZapMessageItem -> "chatZap"
                        }
                    },
                ) { chatItem ->
                    when (chatItem) {
                        is StreamChatItem.ChatMessageItem -> ChatMessageListItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            message = chatItem.message,
                            onNostrUriClick = onNostrUriClick,
                            onProfileClick = onProfileClick,
                            onClick = { onChatMessageClick(chatItem.message) },
                        )

                        is StreamChatItem.ZapMessageItem -> ZapMessageListItem(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            zap = chatItem.zap,
                            onClick = { onZapMessageClick(chatItem.zap) },
                        )
                    }
                }
            }
        }
    } else {
        NoteTagUserLazyColumn(
            modifier = modifier,
            content = state.comment,
            taggedUsers = state.taggedUsers,
            users = if (state.userTaggingState.userTaggingQuery.isNullOrEmpty()) {
                state.userTaggingState.recommendedUsers
            } else {
                state.userTaggingState.searchResults
            },
            userTaggingQuery = state.userTaggingState.userTaggingQuery ?: "",
            onUserClick = { newContent, newTaggedUsers ->
                eventPublisher(LiveStreamContract.UiEvent.OnCommentValueChanged(newContent))
                eventPublisher(LiveStreamContract.UiEvent.TagUser(taggedUser = newTaggedUsers.last()))
                eventPublisher(LiveStreamContract.UiEvent.ToggleSearchUsers(enabled = false))
            },
        )
    }
}

@Composable
private fun LiveChatContent(
    state: LiveStreamContract.UiState,
    listState: LazyListState,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    onProfileClick: (String) -> Unit,
    onNostrUriClick: (String) -> Unit,
    onChatMessageClick: (ChatMessageUi) -> Unit,
    onZapMessageClick: (EventZapUiModel) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val isAtBottom by remember { derivedStateOf { !listState.canScrollBackward } }

    val isChatShowingLatestContent by remember { derivedStateOf { listState.firstVisibleItemIndex < 2 } }

    LaunchedEffect(state.chatItems) {
        if (isChatShowingLatestContent && state.chatItems.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatBackgroundHandleColor)
            .imePadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            LiveChatListOrSearch(
                modifier = Modifier.fillMaxWidth(),
                state = state,
                listState = listState,
                eventPublisher = eventPublisher,
                onProfileClick = onProfileClick,
                onNostrUriClick = onNostrUriClick,
                onChatMessageClick = onChatMessageClick,
                onZapMessageClick = onZapMessageClick,
            )

            androidx.compose.animation.AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomEnd),
                visible = !isAtBottom && !state.userTaggingState.isUserTaggingActive,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut(),
            ) {
                IconButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AppTheme.extraColorScheme.surfaceVariantAlt1),
                    onClick = { coroutineScope.launch { listState.animateScrollToItem(index = 0) } },
                ) {
                    Icon(
                        tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                        modifier = Modifier.padding(8.dp),
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                    )
                }
            }
        }

        if (!state.chatLoading) {
            LiveChatCommentInput(
                state = state,
                onCommentChanged = {
                    eventPublisher(LiveStreamContract.UiEvent.OnCommentValueChanged(it))
                },
                onSendMessage = {
                    eventPublisher(LiveStreamContract.UiEvent.SendMessage(it))
                },
                onUserTaggingModeChanged = { enabled ->
                    eventPublisher(LiveStreamContract.UiEvent.ToggleSearchUsers(enabled = enabled))
                },
                onUserTagSearch = { query ->
                    eventPublisher(LiveStreamContract.UiEvent.SearchUsers(query = query))
                },
            )
        }
    }
}

@Composable
private fun LiveChatEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = R.string.live_stream_empty_chat),
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun LiveChatCommentInput(
    state: LiveStreamContract.UiState,
    onCommentChanged: (TextFieldValue) -> Unit,
    onSendMessage: (String) -> Unit,
    onUserTaggingModeChanged: (Boolean) -> Unit,
    onUserTagSearch: (String) -> Unit,
) {
    val borderColor = AppTheme.extraColorScheme.surfaceVariantAlt1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeWidth,
                )
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NoteOutlinedTextField(
            modifier = Modifier.weight(1.0f),
            value = state.comment,
            onValueChange = onCommentChanged,
            maxLines = 3,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.live_stream_send_comment),
                    maxLines = 1,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    style = AppTheme.typography.bodyMedium,
                )
            },
            textStyle = AppTheme.typography.bodyMedium,
            colors = PrimalDefaults.outlinedTextFieldColors(),
            shape = AppTheme.shapes.extraLarge,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences,
            ),
            taggedUsers = state.taggedUsers,
            onUserTaggingModeChanged = onUserTaggingModeChanged,
            onUserTagSearch = onUserTagSearch,
            keyboardActions = KeyboardActions(
                onSend = { onSendMessage(state.comment.text) },
            ),
        )

        if (state.comment.text.isNotBlank()) {
            IconButton(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(46.dp),
                onClick = { onSendMessage(state.comment.text) },
                enabled = !state.sendingMessage,
            ) {
                if (state.sendingMessage) {
                    PrimalLoadingSpinner(size = 24.dp)
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(id = R.string.live_stream_send_button_title),
                        tint = AppTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageListItem(
    message: ChatMessageUi,
    onNostrUriClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val localUriHandler = LocalUriHandler.current

    val annotatedContent = rememberAnnotatedContent(message = message)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = onClick != null,
                onClick = { onClick?.invoke() },
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        UniversalAvatarThumbnail(
            avatarCdnImage = message.authorProfile.avatarCdnImage,
            avatarSize = 24.dp,
            legendaryCustomization = message.authorProfile.premiumDetails?.legendaryCustomization,
        )

        PrimalClickableText(
            modifier = Modifier.padding(top = 7.dp),
            text = annotatedContent,
            style = AppTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            onClick = { position, _ ->
                val urlAnnotation = annotatedContent.getStringAnnotations(
                    tag = URL_ANNOTATION_TAG,
                    start = position,
                    end = position,
                ).firstOrNull()

                if (urlAnnotation != null) {
                    val uri = urlAnnotation.item
                    if (uri.startsWith("nostr:")) {
                        onNostrUriClick(uri)
                    } else {
                        localUriHandler.openUriSafely(uri)
                    }
                    return@PrimalClickableText
                }

                val profileAnnotation = annotatedContent.getStringAnnotations(
                    tag = "profileId",
                    start = position,
                    end = position,
                ).firstOrNull()

                if (profileAnnotation != null) {
                    onProfileClick(profileAnnotation.item)
                    return@PrimalClickableText
                }

                onClick?.invoke()
            },
        )
    }
}

@Composable
private fun rememberAnnotatedContent(message: ChatMessageUi): AnnotatedString {
    val authorNameColor = AppTheme.colorScheme.onSurface
    val defaultTextColor = if (isAppInDarkPrimalTheme()) {
        AppTheme.extraColorScheme.onSurfaceVariantAlt1
    } else {
        AppTheme.extraColorScheme.onSurfaceVariantAlt2
    }

    val linkStyle = SpanStyle(textDecoration = TextDecoration.Underline)
    val highlightColor = AppTheme.colorScheme.primary

    return remember(message) {
        buildAnnotatedString {
            withStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold, color = authorNameColor),
            ) {
                append(message.authorProfile.authorDisplayName)
            }
            append(" ")

            val renderedContent = renderChatMessageContentAsAnnotatedString(
                message = message,
                highlightColor = highlightColor,
            )

            val messageWithLinks = spannableTextWithLinks(
                text = renderedContent,
                defaultColor = defaultTextColor,
                linkStyle = linkStyle,
            )
            append(messageWithLinks)
        }
    }
}

@Composable
fun ZapMessageListItem(
    zap: EventZapUiModel,
    modifier: Modifier = Modifier,
    isScrollable: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = ZapMessageBorderColor,
                shape = AppTheme.shapes.medium,
            )
            .clip(AppTheme.shapes.medium)
            .clickable(
                enabled = onClick != null,
                onClick = { onClick?.invoke() },
            )
            .background(
                color = if (LocalPrimalTheme.current.isDarkTheme) {
                    Color.Black
                } else {
                    Color.White
                },
            )
            .background(color = ZapMessageBackgroundColor.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        UniversalAvatarThumbnail(
            avatarCdnImage = zap.zapperAvatarCdnImage,
            avatarSize = 24.dp,
            legendaryCustomization = zap.zapperLegendaryCustomization,
        )
        ZapMessageContent(zap = zap, onClick = onClick, isScrollable = isScrollable)
    }
}

@Composable
private fun ZapMessageContent(
    zap: EventZapUiModel,
    onClick: (() -> Unit)?,
    isScrollable: Boolean,
) {
    val localUriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier.padding(top = 1.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        ZapMessageHeader(zap = zap)

        if (!zap.message.isNullOrBlank()) {
            val defaultTextColor = AppTheme.colorScheme.onSurface
            val linkStyle = SpanStyle(textDecoration = TextDecoration.Underline)

            val contentText = remember(zap.message, defaultTextColor) {
                spannableTextWithLinks(
                    text = AnnotatedString(zap.message),
                    defaultColor = defaultTextColor,
                    linkStyle = linkStyle,
                )
            }
            PrimalClickableText(
                modifier = Modifier
                    .run {
                        if (isScrollable) {
                            this.verticalScroll(state = rememberScrollState())
                        } else {
                            this
                        }
                    }
                    .padding(top = 5.dp),
                text = contentText,
                style = AppTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                onClick = { position, _ ->
                    val urlAnnotation = contentText.getStringAnnotations(
                        tag = URL_ANNOTATION_TAG,
                        start = position,
                        end = position,
                    ).firstOrNull()

                    if (urlAnnotation != null) {
                        localUriHandler.openUriSafely(urlAnnotation.item)
                    } else {
                        onClick?.invoke()
                    }
                },
            )
        }
    }
}

@Composable
private fun ZapMessageHeader(zap: EventZapUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BasicText(
            modifier = Modifier.weight(1f),
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = ZapMessageProfileHandleColor, fontWeight = FontWeight.Bold)) {
                    append(zap.zapperName)
                }
                withStyle(style = SpanStyle(color = ZapMessageProfileHandleColor)) {
                    append(" ${stringResource(id = R.string.live_stream_zapped)}")
                }
            },
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 20.sp,
            ),
            autoSize = TextAutoSize.StepBased(
                minFontSize = 4.sp,
                maxFontSize = 16.sp,
            ),
            maxLines = 1,
        )

        Row(
            modifier = Modifier
                .background(
                    color = ZapMessageProfileHandleColor,
                    shape = AppTheme.shapes.extraLarge,
                )
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val numberFormatter = remember { NumberFormat.getInstance() }
            val formattedAmount = remember(zap.amountInSats) {
                numberFormatter.format(zap.amountInSats.toLong())
            }

            IconText(
                modifier = Modifier
                    .alignByBaseline()
                    .padding(end = 2.dp, top = 1.dp),
                text = formattedAmount,
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodySmall.copy(
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                ),
                leadingIcon = PrimalIcons.NavWalletBoltFilled,
                iconSize = 16.sp,
                color = AppTheme.colorScheme.surface,
            )
        }
    }
}

private fun isPlaybackAtLiveEdge(mediaController: MediaController): Boolean {
    val liveOffsetMs = mediaController.currentLiveOffset

    return if (liveOffsetMs != C.TIME_UNSET) {
        liveOffsetMs < LIVE_EDGE_THRESHOLD_MS
    } else if (mediaController.isCurrentMediaItemLive) {
        val duration = mediaController.duration
        if (duration != C.TIME_UNSET) {
            val position = mediaController.currentPosition
            (duration - position) < LIVE_EDGE_THRESHOLD_MS
        } else {
            true
        }
    } else {
        false
    }
}

private fun handleZapProfile(
    profileDetails: ProfileDetailsUi,
    callbacks: LiveStreamContract.ScreenCallbacks,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    context: Context,
) {
    val profileLud16 = profileDetails.internetIdentifier

    if (profileLud16?.isLightningAddress() == true) {
        callbacks.onSendWalletTx(
            DraftTx(
                targetUserId = profileDetails.pubkey,
                targetLud16 = profileLud16,
            ),
        )
    } else {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = context.getString(
                    R.string.wallet_send_payment_error_nostr_user_without_lightning_address,
                    profileDetails.authorDisplayName,
                ),
                duration = SnackbarDuration.Short,
            )
        }
    }
}

private fun spannableTextWithLinks(
    text: AnnotatedString,
    defaultColor: Color,
    linkStyle: SpanStyle,
): AnnotatedString {
    val uriLinks = text.text.detectUrls() + text.text.parseNostrUris()
        .filterNot { it.contains("nprofile") }

    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = defaultColor)) {
            append(text)
        }

        uriLinks.forEach { url ->
            val startIndex = text.text.indexOf(url)
            if (startIndex != -1) {
                addStyle(
                    style = linkStyle,
                    start = startIndex,
                    end = startIndex + url.length,
                )
                addStringAnnotation(
                    tag = URL_ANNOTATION_TAG,
                    annotation = url,
                    start = startIndex,
                    end = startIndex + url.length,
                )
            }
        }
    }
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

private fun renderChatMessageContentAsAnnotatedString(message: ChatMessageUi, highlightColor: Color): AnnotatedString {
    val mentionedUsers = message.nostrUris.filter { it.type == EventUriNostrType.Profile }

    val refinedContent = message.content
        .clearAtSignFromNostrUris()
        .replaceNostrProfileUrisWithHandles(resources = mentionedUsers)

    return buildAnnotatedString {
        append(refinedContent)

        mentionedUsers.forEach {
            checkNotNull(it.referencedUser)
            addProfileAnnotation(
                referencedUser = it.referencedUser,
                content = refinedContent,
                highlightColor = highlightColor,
            )
        }
    }
}

private fun AnnotatedString.Builder.addProfileAnnotation(
    referencedUser: ReferencedUser,
    content: String,
    highlightColor: Color,
) {
    val displayHandle = referencedUser.displayUsername
    var startIndex = content.indexOf(displayHandle)

    while (startIndex >= 0) {
        val endIndex = startIndex + displayHandle.length

        addStyle(
            style = SpanStyle(color = highlightColor),
            start = startIndex,
            end = endIndex,
        )

        addStringAnnotation(
            tag = "profileId",
            annotation = referencedUser.userId,
            start = startIndex,
            end = endIndex,
        )

        startIndex = content.indexOf(displayHandle, startIndex + 1)
    }
}

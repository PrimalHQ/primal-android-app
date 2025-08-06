package net.primal.android.stream

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import net.primal.android.R
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.profile.approvals.FollowsApprovalAlertDialog
import net.primal.android.core.compose.zaps.ArticleTopZapsSection
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.notes.feed.zaps.UnableToZapBottomSheet
import net.primal.android.notes.feed.zaps.ZapBottomSheet
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.zaps.canZap
import net.primal.domain.nostr.ReactionType

private const val LIVE_EDGE_THRESHOLD_MS = 20_000
private const val PLAYER_STATE_UPDATE_INTERVAL_MS = 200L
private const val SEEK_INCREMENT_MS = 10_000L

private enum class LiveStreamDisplaySection {
    Info,
    Chat,
}

@Composable
fun LiveStreamScreen(
    viewModel: LiveStreamViewModel,
    onClose: () -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
) {
    val uiState by viewModel.state.collectAsState()

    LiveStreamScreen(
        state = uiState,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiveStreamScreen(
    state: LiveStreamContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val snackbarHostState = remember { SnackbarHostState() }

    var showCantZapWarning by remember { mutableStateOf(false) }
    if (showCantZapWarning) {
        UnableToZapBottomSheet(
            zappingState = state.zappingState,
            onDismissRequest = { showCantZapWarning = false },
            onGoToWallet = onGoToWallet,
        )
    }

    var showZapOptions by remember { mutableStateOf(false) }
    if (showZapOptions && state.authorProfile != null) {
        ZapBottomSheet(
            onDismissRequest = { showZapOptions = false },
            receiverName = state.authorProfile.authorDisplayName,
            zappingState = state.zappingState,
            onZap = { zapAmount, zapDescription ->
                if (state.zappingState.canZap(zapAmount)) {
                    eventPublisher(
                        LiveStreamContract.UiEvent.ZapStream(
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

    fun invokeZapOptionsOrShowWarning() {
        if (state.zappingState.walletConnected) {
            showZapOptions = true
        } else {
            showCantZapWarning = true
        }
    }

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

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                eventPublisher(LiveStreamContract.UiEvent.OnPlayerStateUpdate(isPlaying = isPlaying))
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                eventPublisher(
                    LiveStreamContract.UiEvent.OnPlayerStateUpdate(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                    ),
                )
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    val latestState by rememberUpdatedState(state)

    LaunchedEffect(exoPlayer) {
        while (true) {
            if (!latestState.playerState.isSeeking) {
                val duration = exoPlayer.duration
                if (duration != C.TIME_UNSET) {
                    val newCurrentTime = exoPlayer.currentPosition.coerceAtLeast(0L)
                    val newTotalDuration = duration.coerceAtLeast(0L)
                    val isAtLiveEdge =
                        latestState.playerState.isLive && (newTotalDuration - newCurrentTime <= LIVE_EDGE_THRESHOLD_MS)

                    if (newCurrentTime != latestState.playerState.currentTime ||
                        newTotalDuration != latestState.playerState.totalDuration ||
                        isAtLiveEdge != latestState.playerState.atLiveEdge
                    ) {
                        eventPublisher(
                            LiveStreamContract.UiEvent.OnPlayerStateUpdate(
                                atLiveEdge = isAtLiveEdge,
                                currentTime = newCurrentTime,
                                totalDuration = newTotalDuration,
                            ),
                        )
                    }
                }
            }
            delay(PLAYER_STATE_UPDATE_INTERVAL_MS.milliseconds)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            LiveStreamContent(
                state = state,
                exoPlayer = exoPlayer,
                eventPublisher = eventPublisher,
                paddingValues = paddingValues,
                onClose = onClose,
                noteCallbacks = noteCallbacks,
                onZapClick = { invokeZapOptionsOrShowWarning() },
            )
        },
    )
}

@Composable
private fun LiveStreamContent(
    state: LiveStreamContract.UiState,
    exoPlayer: ExoPlayer,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    paddingValues: PaddingValues,
    onClose: () -> Unit,
    noteCallbacks: NoteCallbacks,
    onZapClick: () -> Unit,
) {
    var currentSection by remember { mutableStateOf(LiveStreamDisplaySection.Info) }

    if (state.loading) {
        PrimalLoadingSpinner()
    }

    if (state.streamInfo != null && state.authorProfile != null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colorScheme.surface)
                .padding(paddingValues),
        ) {
            item("MediaPlayer") {
                LiveStreamPlayer(
                    exoPlayer = exoPlayer,
                    streamUrl = state.streamInfo.streamUrl,
                    playerState = state.playerState,
                    onPlayPauseClick = {
                        if (exoPlayer.isPlaying) {
                            exoPlayer.pause()
                        } else {
                            exoPlayer.play()
                        }
                    },
                    onRewind = {
                        val newPosition = (exoPlayer.currentPosition - SEEK_INCREMENT_MS).coerceAtLeast(0L)
                        exoPlayer.seekTo(newPosition)
                    },
                    onForward = {
                        if (state.playerState.isLive) {
                            val newPosition = exoPlayer.currentPosition + SEEK_INCREMENT_MS
                            if (newPosition >= state.playerState.totalDuration) {
                                exoPlayer.seekToDefaultPosition()
                            } else {
                                exoPlayer.seekTo(newPosition)
                            }
                        } else {
                            val newPosition = (exoPlayer.currentPosition + SEEK_INCREMENT_MS)
                                .coerceAtMost(state.playerState.totalDuration)
                            exoPlayer.seekTo(newPosition)
                        }
                    },
                    onClose = onClose,
                    onSeek = { positionMs ->
                        eventPublisher(LiveStreamContract.UiEvent.OnSeek(positionMs = positionMs))
                    },
                    onSeekStarted = {
                        eventPublisher(LiveStreamContract.UiEvent.OnSeekStarted)
                    },
                )
            }

            when (currentSection) {
                LiveStreamDisplaySection.Info -> {
                    item("StreamInfoContainer") {
                        StreamInfoDisplay(
                            state = state,
                            eventPublisher = eventPublisher,
                            onZapClick = onZapClick,
                            noteCallbacks = noteCallbacks,
                        )
                    }

                    item("LiveChatHeader") {
                        LiveChatSection(
                            modifier = Modifier.padding(16.dp),
                            onClick = { currentSection = LiveStreamDisplaySection.Chat },
                        )
                    }
                }
                LiveStreamDisplaySection.Chat -> {
                    item("LiveChatContent") {
                        LiveChatContent(
                            onBack = { currentSection = LiveStreamDisplaySection.Info },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.StreamInfoDisplay(
    state: LiveStreamContract.UiState,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    onZapClick: () -> Unit,
    noteCallbacks: NoteCallbacks,
) {
    val streamInfo = state.streamInfo ?: return
    val authorProfile = state.authorProfile ?: return
    val bottomBorderColor = AppTheme.extraColorScheme.surfaceVariantAlt1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                AppTheme.extraColorScheme.surfaceVariantAlt2,
            ).drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height - strokeWidth / 2f
                drawLine(
                    color = bottomBorderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth,
                )
            }.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StreamInfoSection(
            title = streamInfo.title,
            authorProfile = authorProfile,
            viewers = streamInfo.viewers,
            startedAt = streamInfo.startedAt,
            profileStats = state.profileStats,
            isFollowed = state.isFollowed,
            onFollow = {
                state.profileId?.let {
                    eventPublisher(LiveStreamContract.UiEvent.FollowAction(profileId = it))
                }
            },
            onUnfollow = {
                state.profileId?.let {
                    eventPublisher(LiveStreamContract.UiEvent.UnfollowAction(profileId = it))
                }
            },
        )

        if (state.zaps.isNotEmpty()) {
            ArticleTopZapsSection(
                modifier = Modifier.fillMaxWidth(),
                sectionTopSpacing = 0.dp,
                topZaps = state.zaps,
                onZapClick = onZapClick,
                onTopZapsClick = {
                    noteCallbacks.onEventReactionsClick?.invoke(
                        streamInfo.atag,
                        ReactionType.ZAPS,
                        streamInfo.atag,
                    )
                },
            )
        }

        if (streamInfo.description?.isNotBlank() == true) {
            val content = streamInfo.description
            val hashtagColor = AppTheme.colorScheme.primary
            val annotatedContent = remember(content) {
                buildAnnotatedStringWithHashtags(text = content, hashtagColor = hashtagColor)
            }

            PrimalClickableText(
                modifier = Modifier.fillMaxWidth(),
                text = annotatedContent,
                style = AppTheme.typography.bodyLarge.copy(
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                ),
                textSelectable = true,
                onClick = { offset, _ ->
                    annotatedContent.getStringAnnotations("HASHTAG", offset, offset)
                        .firstOrNull()?.let {
                            noteCallbacks.onHashtagClick?.invoke(it.item)
                        }
                },
            )
        }
    }
}

@Composable
private fun LiveChatSection(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.large,
            )
            .clip(AppTheme.shapes.large)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(
                id = R.string.live_stream_title,
            ),
            style = AppTheme.typography.titleLarge.copy(
                fontSize = 16.sp,
                lineHeight = 20.sp,
            ),
            fontWeight = FontWeight.Bold,
            color = AppTheme.colorScheme.onSurface,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = AppTheme.extraColorScheme.surfaceVariantAlt2,
                    shape = AppTheme.shapes.extraLarge,
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = stringResource(
                    id = R.string.live_stream_join_chat,
                ),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun LiveChatContent(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    id = R.string.live_stream_title,
                ),
                style = AppTheme.typography.titleLarge.copy(
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                ),
                fontWeight = FontWeight.Bold,
                color = AppTheme.colorScheme.onSurface,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Chat Settings",
                        tint = AppTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Back to Info",
                        tint = AppTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun buildAnnotatedStringWithHashtags(text: String, hashtagColor: Color): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        val hashtagRegex = "(#\\w+)".toRegex()
        hashtagRegex.findAll(text).forEach { matchResult ->
            addStyle(
                style = SpanStyle(color = hashtagColor),
                start = matchResult.range.first,
                end = matchResult.range.last + 1,
            )
            addStringAnnotation(
                tag = "HASHTAG",
                annotation = matchResult.value.substring(1),
                start = matchResult.range.first,
                end = matchResult.range.last + 1,
            )
        }
    }
}

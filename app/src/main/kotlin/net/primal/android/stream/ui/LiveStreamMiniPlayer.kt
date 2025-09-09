package net.primal.android.stream.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.KeyboardState
import net.primal.android.core.compose.ShadowIcon
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.VideoCloseMini
import net.primal.android.core.compose.icons.primaliconpack.VideoPauseMini
import net.primal.android.core.compose.icons.primaliconpack.VideoPlayMini
import net.primal.android.core.compose.rememberKeyboardState
import net.primal.android.core.video.toggle
import net.primal.android.stream.LiveStreamContract
import net.primal.android.stream.player.LocalStreamState
import net.primal.android.stream.player.SHARED_TRANSITION_LOADING_PLAYER_KEY
import net.primal.android.stream.player.SHARED_TRANSITION_PLAYER_KEY
import net.primal.android.stream.player.VIDEO_ASPECT_RATIO_HEIGHT
import net.primal.android.stream.player.VIDEO_ASPECT_RATIO_WIDTH
import net.primal.android.stream.utils.buildMediaItem
import net.primal.android.theme.AppTheme

internal val PADDING = 16.dp
private const val THIRD = 3
private const val HALF = 2
private val springSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow,
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@UnstableApi
@Composable
fun LiveStreamMiniPlayer(
    modifier: Modifier = Modifier,
    mediaController: MediaController,
    offsetX: Animatable<Float, AnimationVector1D>,
    offsetY: Animatable<Float, AnimationVector1D>,
    isAtBottom: MutableState<Boolean>,
    isAtTop: MutableState<Boolean>,
    state: LiveStreamContract.UiState,
    onExpandStream: () -> Unit,
    onStopStream: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val scope = rememberCoroutineScope()
    val streamState = LocalStreamState.current
    val localConfiguration = LocalConfiguration.current
    val localDensity = LocalDensity.current
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val screenWidthPx = displayMetrics.widthPixels
    val screenHeightPx = displayMetrics.heightPixels
    val playerWidth = if (localConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        screenWidthPx / THIRD
    } else {
        screenWidthPx / HALF
    }

    val playerHeight = playerWidth / (VIDEO_ASPECT_RATIO_WIDTH / VIDEO_ASPECT_RATIO_HEIGHT)
    val statusBarHeight = WindowInsets.statusBars.getTop(localDensity)
    val paddingPx = with(localDensity) { PADDING.toPx() }

    LaunchedEffect(mediaController, state.streamInfo?.streamUrl) {
        if (!mediaController.isPlaying && mediaController.currentMediaItem == null) {
            state.streamInfo?.streamUrl?.let { streamUrl ->
                mediaController.setMediaItem(buildMediaItem(state.naddr, streamUrl, state.streamInfo))
                mediaController.prepare()
                mediaController.playWhenReady = true
            }
        }
    }

    val minSafeY by remember {
        derivedStateOf {
            if (streamState.topBarHeight == 0) {
                statusBarHeight + paddingPx
            } else {
                streamState.topBarHeight + paddingPx
            }
        }
    }

    val maxSafeY by remember {
        derivedStateOf { screenHeightPx - streamState.bottomBarHeight - playerHeight - paddingPx }
    }

    adjustPositionWithKeyboard(offsetY, screenHeightPx, playerHeight, paddingPx)

    LaunchedEffect(streamState.bottomBarHeight, streamState.topBarHeight) {
        when {
            isAtBottom.value -> launch { offsetY.animateTo(targetValue = maxSafeY, animationSpec = springSpec) }
            isAtTop.value -> launch { offsetY.animateTo(targetValue = minSafeY, animationSpec = springSpec) }
            else -> {
                val targetY = offsetY.value.coerceIn(
                    minimumValue = minSafeY,
                    maximumValue = maxSafeY,
                )
                launch { offsetY.animateTo(targetValue = targetY, animationSpec = springSpec) }
            }
        }
    }
    AnimatedVisibility(
        visible = !streamState.isHidden(),
        enter = fadeIn(animationSpec = tween(durationMillis = 400)),
        exit = fadeOut(animationSpec = tween(durationMillis = 50)),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = modifier
                    .offset {
                        IntOffset(
                            offsetX.value.toInt(),
                            offsetY.value.toInt(),
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                val maxOffsetX = (screenWidthPx - playerWidth - paddingPx).coerceAtLeast(0f)

                                val snapToRight = offsetX.value < screenWidthPx / 4

                                val targetX = if (snapToRight) paddingPx else maxOffsetX

                                val targetY = offsetY.value.coerceIn(
                                    minimumValue = minSafeY,
                                    maximumValue = maxSafeY,
                                )
                                scope.launch { offsetX.animateTo(targetX, animationSpec = springSpec) }
                                scope.launch { offsetY.animateTo(targetY, animationSpec = springSpec) }

                                isAtBottom.value = targetY == maxSafeY
                                isAtTop.value = targetY == minSafeY
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                isAtBottom.value = false
                                isAtTop.value = false
                                scope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                    offsetY.snapTo(offsetY.value + dragAmount.y)
                                }
                            },
                        )
                    }
                    .size(
                        width = with(localDensity) { playerWidth.toDp() },
                        height = with(localDensity) { playerHeight.toDp() },
                    )
                    .clip(AppTheme.shapes.large),
            ) {
                with(sharedTransitionScope) {
                    PlayerBox(
                        modifier = Modifier.sharedElement(
                            sharedContentState = rememberSharedContentState(key = SHARED_TRANSITION_PLAYER_KEY),
                            animatedVisibilityScope = animatedVisibilityScope,
                        ),
                        loadingModifier = Modifier.sharedElement(
                            sharedContentState = rememberSharedContentState(key = SHARED_TRANSITION_LOADING_PLAYER_KEY),
                            animatedVisibilityScope = animatedVisibilityScope,
                        ),
                        mediaController = mediaController,
                        state = state,
                    )

                    PlayerControls(
                        onTogglePlayer = { mediaController.toggle() },
                        isLoading = state.playerState.isLoading,
                        isPlaying = mediaController.isPlaying,
                        isStreamUnavailable = state.isStreamUnavailable,
                        onStopStream = onStopStream,
                        onExpandStream = onExpandStream,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerControls(
    onExpandStream: () -> Unit,
    onTogglePlayer: () -> Unit,
    isPlaying: Boolean,
    isLoading: Boolean,
    isStreamUnavailable: Boolean,
    onStopStream: () -> Unit,
) {
    val playPauseIcon = remember(isPlaying) {
        if (isPlaying) {
            PrimalIcons.VideoPauseMini
        } else {
            PrimalIcons.VideoPlayMini
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onExpandStream() },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = when {
                isStreamUnavailable -> Arrangement.End
                isLoading -> Arrangement.End
                else -> Arrangement.SpaceBetween
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!isLoading && !isStreamUnavailable) {
                IconButton(onClick = onTogglePlayer) {
                    ShadowIcon(
                        modifier = Modifier.size(36.dp),
                        tint = Color.White,
                        imageVector = playPauseIcon,
                        contentDescription = stringResource(id = R.string.accessibility_play_pause),
                    )
                }
            }
            IconButton(onClick = onStopStream) {
                ShadowIcon(
                    modifier = Modifier.size(36.dp),
                    tint = Color.White,
                    imageVector = PrimalIcons.VideoCloseMini,
                    contentDescription = stringResource(id = R.string.accessibility_close),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun adjustPositionWithKeyboard(
    offsetY: Animatable<Float, AnimationVector1D>,
    screenHeightPx: Int,
    playerHeight: Float,
    paddingPx: Float,
): KeyboardState {
    val keyboardState by rememberKeyboardState()

    var beforeKeyboardOffsetY by remember { mutableFloatStateOf(0f) }
    var returnOffsetYAfterKeyboard by remember { mutableStateOf(false) }

    fun maxSafeKeyboardY() = screenHeightPx - playerHeight - paddingPx - keyboardState.heightPx

    LaunchedEffect(keyboardState) {
        if (keyboardState.isVisible) {
            if (offsetY.value > maxSafeKeyboardY()) {
                if (!returnOffsetYAfterKeyboard) {
                    beforeKeyboardOffsetY = offsetY.value
                }
                returnOffsetYAfterKeyboard = true
                offsetY.snapTo(maxSafeKeyboardY())
            }
        } else {
            if (returnOffsetYAfterKeyboard) {
                launch { offsetY.animateTo(targetValue = beforeKeyboardOffsetY, animationSpec = springSpec) }
                returnOffsetYAfterKeyboard = false
            }
        }
    }

    return keyboardState
}

@UnstableApi
@Composable
private fun PlayerBox(
    mediaController: MediaController,
    state: LiveStreamContract.UiState,
    modifier: Modifier = Modifier,
    loadingModifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .clip(AppTheme.shapes.large)
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (state.isStreamUnavailable || state.playerState.isVideoFinished) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                val messageText = if (state.playerState.isVideoFinished) {
                    stringResource(id = R.string.live_stream_video_ended)
                } else {
                    stringResource(id = R.string.live_stream_recording_not_available)
                }

                Text(
                    text = messageText,
                    color = Color.White,
                    style = AppTheme.typography.bodySmall,
                )
            }
        } else {
            PlayerSurface(
                modifier = modifier
                    .clip(AppTheme.shapes.large)
                    .matchParentSize(),
                player = mediaController,
                surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            )

            if (state.playerState.isLoading) {
                StreamPlayerLoadingIndicator(
                    modifier = loadingModifier
                        .matchParentSize()
                        .clip(AppTheme.shapes.large)
                        .background(AppTheme.colorScheme.background),
                )
            }
        }
    }
}

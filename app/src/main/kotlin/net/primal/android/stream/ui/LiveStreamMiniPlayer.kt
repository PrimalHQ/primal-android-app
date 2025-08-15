package net.primal.android.stream.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.KeyboardState
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.animatableSaver
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.VideoCloseMini
import net.primal.android.core.compose.icons.primaliconpack.VideoPauseMini
import net.primal.android.core.compose.icons.primaliconpack.VideoPlayMini
import net.primal.android.core.compose.rememberKeyboardState
import net.primal.android.core.video.toggle
import net.primal.android.stream.LiveStreamContract
import net.primal.android.stream.player.LocalStreamState
import net.primal.android.theme.AppTheme

private const val VIDEO_ASPECT_RATIO_WIDTH = 16f
private const val VIDEO_ASPECT_RATIO_HEIGHT = 9f

private val PADDING = 16.dp
private val springSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow,
)

@OptIn(ExperimentalLayoutApi::class)
@UnstableApi
@Composable
fun LiveStreamMiniPlayer(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer,
    state: LiveStreamContract.UiState,
    onExpandStream: () -> Unit,
    onStopStream: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val streamState = LocalStreamState.current
    val localDensity = LocalDensity.current
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val screenWidthPx = displayMetrics.widthPixels
    val screenHeightPx = displayMetrics.heightPixels
    val playerWidth = screenWidthPx / 2
    val playerHeight = playerWidth / (VIDEO_ASPECT_RATIO_WIDTH / VIDEO_ASPECT_RATIO_HEIGHT)
    val statusBarHeight = WindowInsets.statusBars.getTop(localDensity)
    val paddingPx = with(localDensity) { PADDING.toPx() }

    var controlsOverlayVisibility by remember { mutableStateOf(false) }

    LaunchedEffect(exoPlayer) {
        if (!exoPlayer.isPlaying) {
            state.streamInfo?.streamUrl?.let { streamUrl ->
                val mediaItem = MediaItem.fromUri(streamUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
        }
    }

    LaunchedEffect(controlsOverlayVisibility) {
        if (controlsOverlayVisibility) {
            launch {
                delay(3.seconds)
                controlsOverlayVisibility = false
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

    val offsetX = rememberSaveable(saver = animatableSaver()) { Animatable(paddingPx) }
    val offsetY = rememberSaveable(saver = animatableSaver()) { Animatable(maxSafeY) }

    adjustPositionWithKeyboard(offsetY, screenHeightPx, playerHeight, paddingPx)

    AnimatedVisibility(
        visible = !streamState.isHidden(),
        enter = fadeIn(animationSpec = tween(durationMillis = 400)),
        exit = fadeOut(animationSpec = tween(durationMillis = 50)),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LaunchedEffect(streamState.bottomBarHeight, streamState.topBarHeight) {
                val targetY = offsetY.value.coerceIn(
                    minimumValue = minSafeY,
                    maximumValue = maxSafeY,
                )
                launch { offsetY.animateTo(targetValue = targetY, animationSpec = springSpec) }
            }

            Box(
                modifier = modifier
                    .offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }
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
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
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
                    .clip(AppTheme.shapes.large)
                    .background(AppTheme.colorScheme.background)
                    .clickable { controlsOverlayVisibility = true },
            ) {
                PlayerBox(exoPlayer = exoPlayer, state = state)

                PlayerControls(
                    controlsOverlayVisibility = controlsOverlayVisibility,
                    onTogglePlayer = { exoPlayer.toggle() },
                    isPlaying = exoPlayer.isPlaying,
                    onStopStream = onStopStream,
                    onExpandStream = onExpandStream,
                )
            }
        }
    }
}

@Composable
private fun PlayerControls(
    controlsOverlayVisibility: Boolean,
    onExpandStream: () -> Unit,
    onTogglePlayer: () -> Unit,
    isPlaying: Boolean,
    onStopStream: () -> Unit,
) {
    val playPauseIcon = remember(isPlaying) {
        if (isPlaying) {
            PrimalIcons.VideoPauseMini
        } else {
            PrimalIcons.VideoPlayMini
        }
    }

    AnimatedVisibility(
        visible = controlsOverlayVisibility,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onExpandStream() },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onTogglePlayer) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        tint = Color.White,
                        imageVector = playPauseIcon,
                        contentDescription = stringResource(id = R.string.accessibility_play_pause),
                    )
                }
                IconButton(onClick = onStopStream) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        tint = Color.White,
                        imageVector = PrimalIcons.VideoCloseMini,
                        contentDescription = null,
                    )
                }
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
private fun PlayerBox(exoPlayer: ExoPlayer, state: LiveStreamContract.UiState) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        PlayerSurface(
            modifier = Modifier.matchParentSize(),
            player = exoPlayer,
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
        )

        if (state.playerState.isBuffering && !state.playerState.isPlaying) {
            PrimalLoadingSpinner()
        }
    }
}

package net.primal.android.core.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import java.util.Locale
import java.util.concurrent.TimeUnit
import net.primal.android.R
import net.primal.android.theme.AppTheme

private const val SECONDS_IN_A_MINUTE = 60

@Composable
fun PrimalSeekBar(
    progress: Float,
    isInteractive: Boolean,
    onScrub: (Float) -> Unit,
    onScrubEnd: () -> Unit,
    modifier: Modifier = Modifier,
    bufferedProgress: Float = 0f,
    totalDurationMs: Long = 0L,
    currentTimeMs: Long = 0L,
    touchableAreaHeight: Dp = 32.dp,
    trackHeight: Dp = 3.dp,
    thumbRadius: Dp = 8.dp,
    activeTrackColor: Color = AppTheme.colorScheme.primary,
    inactiveTrackColor: Color = Color.White.copy(alpha = 0.3f),
) {
    val accessibleDescription = stringResource(
        id = R.string.accessibility_primal_seek_bar,
        formatDuration(currentTimeMs),
        formatDuration(totalDurationMs),
    )

    var isDragging by remember { mutableStateOf(false) }
    var visualTarget by remember { mutableFloatStateOf(progress) }

    LaunchedEffect(progress, isDragging) {
        if (!isDragging) visualTarget = progress
    }

    val visualProgress by animateFloatAsState(
        targetValue = visualTarget,
        animationSpec = if (isDragging) snap() else spring(),
        label = "SeekBarVisualProgress",
    )

    Box(
        modifier = modifier
            .systemGestureExclusion()
            .semantics(mergeDescendants = true) {
                contentDescription = accessibleDescription
                if (isInteractive) {
                    setProgress { target ->
                        visualTarget = target
                        onScrub(target)
                        onScrubEnd()
                        true
                    }
                }
            }
            .primalSeekBarGestures(
                isInteractive = isInteractive,
                onScrub = onScrub,
                onScrubEnd = onScrubEnd,
                onDraggingStateChange = { isDragging = it },
                onVisualTargetChange = { visualTarget = it },
            )
            .fillMaxWidth()
            .height(touchableAreaHeight),
        contentAlignment = Alignment.Center,
    ) {
        SeekBarTrack(
            visualProgress = visualProgress,
            bufferedProgress = bufferedProgress,
            isInteractive = isInteractive,
            trackHeight = trackHeight,
            thumbRadius = thumbRadius,
            activeTrackColor = activeTrackColor,
            inactiveTrackColor = inactiveTrackColor,
        )
    }
}

private fun Modifier.primalSeekBarGestures(
    isInteractive: Boolean,
    onScrub: (Float) -> Unit,
    onScrubEnd: () -> Unit,
    onDraggingStateChange: (Boolean) -> Unit,
    onVisualTargetChange: (Float) -> Unit,
): Modifier =
    this
        .pointerInput(isInteractive) {
            detectTapGestures { offset ->
                val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                onDraggingStateChange(false)
                onVisualTargetChange(newProgress)
                onScrub(newProgress)
                onScrubEnd()
            }
        }
        .pointerInput(isInteractive) {
            detectDragGestures(
                onDragStart = { offset ->
                    onDraggingStateChange(true)
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onVisualTargetChange(newProgress)
                    onScrub(newProgress)
                },
                onDrag = { change, _ ->
                    change.consume()
                    val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                    onVisualTargetChange(newProgress)
                    onScrub(newProgress)
                },
                onDragEnd = {
                    onDraggingStateChange(false)
                    onScrubEnd()
                },
                onDragCancel = {
                    onDraggingStateChange(false)
                    onScrubEnd()
                },
            )
        }

@Composable
private fun SeekBarTrack(
    modifier: Modifier = Modifier,
    visualProgress: Float,
    bufferedProgress: Float,
    isInteractive: Boolean,
    trackHeight: Dp,
    thumbRadius: Dp,
    inactiveTrackColor: Color,
    activeTrackColor: Color,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val thumbColor = activeTrackColor

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight),
    ) {
        val trackY = center.y
        val trackStroke = trackHeight.toPx()
        val canvasWidth = size.width

        drawLine(
            color = inactiveTrackColor,
            start = Offset(0f, trackY),
            end = Offset(canvasWidth, trackY),
            strokeWidth = trackStroke,
            cap = StrokeCap.Round,
        )

        val progressPx = (visualProgress * canvasWidth).coerceIn(0f, canvasWidth)
        val bufferedProgressPx = (bufferedProgress * canvasWidth).coerceIn(0f, canvasWidth)
        val (bufferedStartPx, bufferedEndPx) = if (isRtl) {
            Pair(canvasWidth - progressPx, canvasWidth - bufferedProgressPx)
        } else {
            Pair(progressPx, bufferedProgressPx)
        }
        drawLine(
            color = activeTrackColor.copy(alpha = 0.5f),
            start = Offset(bufferedStartPx, trackY),
            end = Offset(bufferedEndPx, trackY),
            strokeWidth = trackStroke,
            cap = StrokeCap.Round,
        )

        val (startPx, endPx) = if (isRtl) {
            Pair(canvasWidth, canvasWidth - progressPx)
        } else {
            Pair(0f, progressPx)
        }
        drawLine(
            color = activeTrackColor,
            start = Offset(startPx, trackY),
            end = Offset(endPx, trackY),
            strokeWidth = trackStroke,
            cap = StrokeCap.Round,
        )

        if (isInteractive) {
            val thumbX = if (isRtl) canvasWidth - progressPx else progressPx
            drawCircle(
                color = thumbColor,
                radius = thumbRadius.toPx(),
                center = Offset(thumbX, trackY),
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % SECONDS_IN_A_MINUTE
    return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
}

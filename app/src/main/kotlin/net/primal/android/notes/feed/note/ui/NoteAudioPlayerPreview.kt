package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import net.primal.android.audio.player.AudioPlayerState
import net.primal.android.audio.player.LocalAudioPlayerState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.VideoPauseMini
import net.primal.android.core.compose.icons.primaliconpack.VideoPlayMini
import net.primal.android.theme.AppTheme
import net.primal.core.utils.extractTLD

private val CardHeight = 90.dp
private val PlayButtonSize = 42.dp
private const val WaveformBarCount = 40
private val WaveformBarSpacing = 2.dp
private const val WaveformMinHeight = 0.2f
private const val WaveformHeightRange = 0.8f
private const val MILLIS_PER_SECOND = 1000
private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_HOUR = 3600

@Composable
fun NoteAudioPlayerPreview(
    modifier: Modifier = Modifier,
    title: String?,
    url: String,
) {
    val audioState = LocalAudioPlayerState.current
    val isActive = audioState.isActiveForUrl(url)
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }

    val progress = if (isActive) audioState.progress else 0f
    val displayProgress = if (isScrubbing) scrubProgress else progress
    val durationMs = if (isActive) audioState.durationMs else 0L
    val displayPositionMs = if (isScrubbing) {
        (scrubProgress * durationMs).toLong()
    } else if (isActive) {
        audioState.currentPositionMs
    } else {
        0L
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(CardHeight)
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt3,
                shape = AppTheme.shapes.small,
            )
            .border(
                width = 0.5.dp,
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.small,
            )
            .clip(AppTheme.shapes.small)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayPauseButton(
            audioState = audioState,
            isActive = isActive,
            title = title,
            url = url,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            ProgressWaveform(
                modifier = Modifier.fillMaxWidth(),
                seed = url.hashCode(),
                progress = displayProgress,
                isInteractive = isActive && durationMs > 0L,
                onScrub = { scrubValue ->
                    isScrubbing = true
                    scrubProgress = scrubValue
                },
                onScrubEnd = {
                    val seekPosition = (scrubProgress * durationMs).toLong()
                    audioState.seekTo(seekPosition)
                    isScrubbing = false
                },
            )

            Spacer(modifier = Modifier.height(6.dp))

            AudioInfoRow(
                title = title,
                url = url,
                displayPositionMs = displayPositionMs,
                durationMs = durationMs,
            )
        }
    }
}

@Composable
private fun PlayPauseButton(
    audioState: AudioPlayerState,
    isActive: Boolean,
    title: String?,
    url: String,
) {
    Box(
        modifier = Modifier
            .size(PlayButtonSize)
            .background(
                color = AppTheme.colorScheme.secondary,
                shape = CircleShape,
            )
            .clip(CircleShape)
            .clickable {
                if (isActive && audioState.isPlaying) {
                    audioState.pause()
                } else if (isActive) {
                    audioState.resume()
                } else {
                    audioState.play(url = url, title = title, artist = url.extractTLD())
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (isActive && audioState.isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
        } else {
            val isShowingPause = isActive && audioState.playWhenReady
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = if (isShowingPause) PrimalIcons.VideoPauseMini else PrimalIcons.VideoPlayMini,
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun AudioInfoRow(
    title: String?,
    url: String,
    displayPositionMs: Long,
    durationMs: Long,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val displayTitle = title ?: url.extractTLD() ?: "Audio"
        Text(
            modifier = Modifier.weight(1f),
            text = displayTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = AppTheme.colorScheme.onSurface,
            style = AppTheme.typography.bodyMedium,
            fontSize = 14.sp,
        )

        if (durationMs > 0L) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${formatDuration(displayPositionMs)} / ${formatDuration(durationMs)}",
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                style = AppTheme.typography.bodySmall,
                fontSize = 11.sp,
            )
        }
    }

    val tld = url.extractTLD()
    if (tld != null) {
        Text(
            text = tld,
            maxLines = 1,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            style = AppTheme.typography.bodySmall,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ProgressWaveform(
    modifier: Modifier = Modifier,
    seed: Int,
    progress: Float,
    isInteractive: Boolean = false,
    onScrub: (Float) -> Unit = {},
    onScrubEnd: () -> Unit = {},
) {
    val barHeights = remember(seed) {
        val random = Random(seed)
        List(WaveformBarCount) { WaveformMinHeight + random.nextFloat() * WaveformHeightRange }
    }

    Row(
        modifier = modifier
            .height(24.dp)
            .then(
                if (isInteractive) {
                    Modifier
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                                onScrub(newProgress)
                                onScrubEnd()
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                                    onScrub(newProgress)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                                    onScrub(newProgress)
                                },
                                onDragEnd = { onScrubEnd() },
                                onDragCancel = { onScrubEnd() },
                            )
                        }
                } else {
                    Modifier
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WaveformBarSpacing),
    ) {
        barHeights.forEachIndexed { index, heightFraction ->
            val barProgress = index.toFloat() / barHeights.size
            val isPlayed = barProgress < progress
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp * heightFraction)
                    .background(
                        color = if (isPlayed) {
                            AppTheme.colorScheme.secondary
                        } else {
                            AppTheme.colorScheme.secondary.copy(alpha = 0.3f)
                        },
                        shape = AppTheme.shapes.extraSmall,
                    ),
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / MILLIS_PER_SECOND
    val hours = totalSeconds / SECONDS_PER_HOUR
    val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

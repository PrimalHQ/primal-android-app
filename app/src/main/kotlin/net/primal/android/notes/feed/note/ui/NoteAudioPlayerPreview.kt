package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlin.random.Random
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Play
import net.primal.android.core.video.rememberAudioController
import net.primal.android.theme.AppTheme
import net.primal.core.utils.extractTLD

private val CardHeight = 90.dp
private val PlayButtonSize = 42.dp
private const val WaveformBarCount = 40
private val WaveformBarWidth = 3.dp
private val WaveformBarSpacing = 2.dp
private const val WaveformMinHeight = 0.2f
private const val WaveformHeightRange = 0.8f

@Suppress("LongMethod")
@Composable
fun NoteAudioPlayerPreview(
    modifier: Modifier = Modifier,
    eventId: String,
    title: String?,
    url: String,
) {
    val controller = rememberAudioController(mediaId = eventId)

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
            .clickable {
                controller?.let { c ->
                    val mediaItem = MediaItem.Builder()
                        .setUri(url)
                        .setMediaId(eventId)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(title)
                                .setArtist(url.extractTLD())
                                .build(),
                        )
                        .build()
                    c.clearMediaItems()
                    c.setMediaItem(mediaItem)
                    c.prepare()
                    c.play()
                }
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(PlayButtonSize)
                .background(
                    color = AppTheme.colorScheme.secondary,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = PrimalIcons.Play,
                contentDescription = null,
                tint = AppTheme.colorScheme.onSecondary,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            DecorativeWaveform(
                modifier = Modifier.fillMaxWidth(),
                seed = url.hashCode(),
            )

            Spacer(modifier = Modifier.height(6.dp))

            val displayTitle = title ?: url.substringAfterLast("/").substringBefore("?")
            Text(
                text = displayTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = AppTheme.colorScheme.onSurface,
                style = AppTheme.typography.bodyMedium,
                fontSize = 14.sp,
            )

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
    }
}

@Composable
private fun DecorativeWaveform(modifier: Modifier = Modifier, seed: Int) {
    val barHeights = remember(seed) {
        val random = Random(seed)
        List(WaveformBarCount) { WaveformMinHeight + random.nextFloat() * WaveformHeightRange }
    }

    Row(
        modifier = modifier.height(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WaveformBarSpacing),
    ) {
        barHeights.forEach { heightFraction ->
            Box(
                modifier = Modifier
                    .width(WaveformBarWidth)
                    .height(24.dp * heightFraction)
                    .background(
                        color = AppTheme.colorScheme.secondary.copy(alpha = 0.7f),
                        shape = AppTheme.shapes.extraSmall,
                    ),
            )
        }
    }
}

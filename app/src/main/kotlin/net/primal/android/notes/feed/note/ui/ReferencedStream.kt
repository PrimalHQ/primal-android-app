package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.asFromNowFormat
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Follow
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.stream.ui.StreamLiveIndicator
import net.primal.android.theme.AppTheme
import net.primal.domain.links.ReferencedStream
import net.primal.domain.streams.StreamStatus

@Composable
fun ReferencedStream(
    modifier: Modifier = Modifier,
    stream: ReferencedStream,
    onClick: (naddr: String) -> Unit,
    onProfileClick: (profileId: String) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                shape = AppTheme.shapes.medium,
                color = AppTheme.extraColorScheme.surfaceVariantAlt3,
            )
            .padding(12.dp)
            .clickable { onClick(stream.naddr) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        UniversalAvatarThumbnail(
            isLive = stream.mainHostIsLive,
            avatarCdnImage = stream.mainHostAvatarCdnImage,
            legendaryCustomization = stream.mainHostLegendProfile?.asLegendaryCustomization(),
            onClick = { onProfileClick(stream.mainHostId) },
        )

        StreamInfo(stream = stream)
    }
}

@Composable
fun ReferencedNotificationStream(
    modifier: Modifier = Modifier,
    stream: ReferencedStream,
    onClick: (naddr: String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                shape = AppTheme.shapes.medium,
                color = AppTheme.extraColorScheme.surfaceVariantAlt3,
            )
            .padding(12.dp)
            .clickable { onClick(stream.naddr) },
    ) {
        StreamInfo(stream = stream, showHostInfo = false)
    }
}

@Composable
private fun StreamInfo(stream: ReferencedStream, showHostInfo: Boolean = true) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showHostInfo) {
            NostrUserText(
                displayName = stream.mainHostName,
                internetIdentifier = stream.mainHostInternetIdentifier,
                legendaryCustomization = stream.mainHostLegendProfile?.asLegendaryCustomization(),
            )
        }

        stream.title?.let { title ->
            Text(
                text = title,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        when (stream.status) {
            StreamStatus.PLANNED -> PlannedStatusIndicator(startsAt = stream.startedAt)

            StreamStatus.LIVE -> LiveStatusIndicator(
                startedAt = stream.startedAt,
                viewers = stream.currentParticipants,
            )

            StreamStatus.ENDED -> EndedStatusIndicator(
                endedAt = stream.endedAt,
                duration = stream.duration,
                viewers = stream.totalParticipants ?: stream.currentParticipants,
            )
        }
    }
}

@Composable
private fun PlannedStatusIndicator(modifier: Modifier = Modifier, startsAt: Long?) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StreamLiveIndicator(isLive = false)

        startsAt?.let {
            val fromNow = Instant.ofEpochSecond(startsAt).asFromNowFormat()
            Text(
                text = "${stringResource(id = R.string.live_stream_starting)} $fromNow",
                color = if (isAppInDarkPrimalTheme()) {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt3
                } else {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt2
                },
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun LiveStatusIndicator(
    modifier: Modifier = Modifier,
    startedAt: Long?,
    viewers: Int?,
) {
    val isDarkTheme = isAppInDarkPrimalTheme()
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StreamLiveIndicator(isLive = true, textColor = AppTheme.colorScheme.onPrimary)

        startedAt?.let {
            Text(
                text = stringResource(
                    id = R.string.live_stream_started_at,
                    Instant.ofEpochSecond(startedAt).asBeforeNowFormat(),
                ),
                color = if (isDarkTheme) {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt3
                } else {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt2
                },
                style = AppTheme.typography.bodyMedium,
            )
        }

        viewers?.let {
            IconText(
                text = numberFormat.format(viewers),
                leadingIcon = PrimalIcons.Follow,
                iconSize = 14.sp,
                color = if (isDarkTheme) {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt3
                } else {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt2
                },
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun EndedStatusIndicator(
    modifier: Modifier = Modifier,
    endedAt: Long?,
    duration: Long?,
    viewers: Int?,
) {
    val isDarkTheme = isAppInDarkPrimalTheme()

    val numberFormat = remember { NumberFormat.getNumberInstance() }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StreamLiveIndicator(isLive = false, hideTextIfNotLive = true)

        endedAt?.let {
            Text(
                text = stringResource(
                    id = R.string.live_stream_streamed,
                    Instant.ofEpochSecond(endedAt).asBeforeNowFormat(),
                ),
                color = if (isDarkTheme) {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt3
                } else {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt2
                },
                style = AppTheme.typography.bodyMedium,
            )
        }

        duration?.let {
            val duration = Duration.of(duration, ChronoUnit.SECONDS)

            Text(
                text = "${duration.toHours()}:${duration.toMinutes() % 60}",
                color = if (isDarkTheme) {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt3
                } else {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt2
                },
                style = AppTheme.typography.bodyMedium,
            )
        }

        viewers?.let {
            IconText(
                text = numberFormat.format(viewers),
                leadingIcon = PrimalIcons.Follow,
                iconSize = 14.sp,
                color = if (isDarkTheme) {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt3
                } else {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt2
                },
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}

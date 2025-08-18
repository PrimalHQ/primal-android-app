package net.primal.android.stream.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.time.Instant
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AdvancedSearch
import net.primal.android.core.compose.icons.primaliconpack.Follow
import net.primal.android.core.compose.icons.primaliconpack.Info
import net.primal.android.theme.AppTheme

private val LiveIndicatorColor = Color(0xFFEE0000)
private val NotLiveIndicatorColor = Color(0xFFAAAAAA)

@Composable
fun StreamInfoSection(
    title: String,
    viewers: Int,
    startedAt: Long?,
    isLive: Boolean,
    onChatSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    isKeyboardVisible: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (!isKeyboardVisible) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = title,
                    style = AppTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 20.sp,
                    ),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    IconButton(
                        onClick = onChatSettingsClick,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = PrimalIcons.AdvancedSearch,
                            contentDescription = "Chat Settings",
                            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        )
                    }

                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = PrimalIcons.Info,
                            contentDescription = "Information",
                            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        )
                    }
                }
            }
        }

        StreamMetaData(
            isLive = isLive,
            startedAt = startedAt,
            viewers = viewers,
        )
    }
}

@Composable
fun StreamMetaData(
    modifier: Modifier = Modifier,
    isLive: Boolean,
    startedAt: Long?,
    viewers: Int,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StreamLiveIndicator(isLive = isLive)

        if (startedAt != null) {
            Text(
                text = stringResource(
                    id = R.string.live_stream_started_at,
                    Instant.ofEpochSecond(startedAt).asBeforeNowFormat(),
                ),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                style = AppTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                ),
            )
        }
        IconText(
            text = numberFormat.format(viewers),
            leadingIcon = PrimalIcons.Follow,
            iconSize = 12.sp,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            style = AppTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 16.sp,
            ),
        )
    }
}

@Composable
fun StreamLiveIndicator(modifier: Modifier = Modifier, isLive: Boolean) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    color = if (isLive) LiveIndicatorColor else NotLiveIndicatorColor,
                    shape = CircleShape,
                ),
        )
        Text(
            text = stringResource(id = R.string.live_stream_live_indicator),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            style = AppTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 14.sp,
            ),
        )
    }
}

package net.primal.android.stream.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.core.compose.bubble.AnchorHandle
import net.primal.android.core.compose.bubble.anchor
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AdvancedSearch
import net.primal.android.core.compose.icons.primaliconpack.Info
import net.primal.android.theme.AppTheme

@Composable
fun StreamInfoSection(
    title: String,
    viewers: Int,
    startedAt: Long?,
    isLive: Boolean,
    onChatSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    streamControlAnchorHandle: AnchorHandle,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconButton(
                    onClick = onChatSettingsClick,
                    modifier = Modifier
                        .size(24.dp)
                        .anchor(handle = streamControlAnchorHandle),
                ) {
                    Icon(
                        imageVector = PrimalIcons.AdvancedSearch,
                        contentDescription = "Chat Settings",
                        tint = if (isAppInDarkPrimalTheme()) {
                            AppTheme.extraColorScheme.onSurfaceVariantAlt1
                        } else {
                            AppTheme.extraColorScheme.onSurfaceVariantAlt2
                        },
                    )
                }

                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = PrimalIcons.Info,
                        contentDescription = "Information",
                        tint = if (isAppInDarkPrimalTheme()) {
                            AppTheme.extraColorScheme.onSurfaceVariantAlt1
                        } else {
                            AppTheme.extraColorScheme.onSurfaceVariantAlt2
                        },
                    )
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

package net.primal.android.stream.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.time.Instant
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Follow
import net.primal.android.theme.AppTheme

@Composable
fun StreamMetaData(
    modifier: Modifier = Modifier,
    isLive: Boolean,
    startedAt: Long?,
    viewers: Int,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    val textColor = if (isAppInDarkPrimalTheme()) {
        AppTheme.extraColorScheme.onSurfaceVariantAlt3
    } else {
        AppTheme.extraColorScheme.onSurfaceVariantAlt2
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StreamLiveIndicator(isLive = isLive, textColor = textColor, fontSize = 15.sp, lineHeight = 16.sp)

        if (startedAt != null) {
            Text(
                text = stringResource(
                    id = R.string.live_stream_started_at_prefix,
                    Instant.ofEpochSecond(startedAt).asBeforeNowFormat(
                        shortFormat = true,
                        agoSuffixOnShortFormat = true,
                    ),
                ),
                color = textColor,
                style = AppTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 16.sp,
                ),
            )
        }
        IconText(
            text = numberFormat.format(viewers),
            leadingIcon = PrimalIcons.Follow,
            iconSize = 14.sp,
            color = textColor,
            style = AppTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                lineHeight = 16.sp,
            ),
        )
    }
}

package net.primal.android.stream.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme
import net.primal.android.theme.AppTheme

private val LiveIndicatorColor = Color(0xFFEE0000)
private val NotLiveIndicatorColorDark = Color(0xFF757575)
private val NotLiveIndicatorColorLight = Color(0xFF666666)

@Composable
fun StreamLiveIndicator(
    modifier: Modifier = Modifier,
    isLive: Boolean,
    hideTextIfNotLive: Boolean = false,
    textColor: Color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    color = if (isLive) {
                        LiveIndicatorColor
                    } else {
                        if (isAppInDarkPrimalTheme()) {
                            NotLiveIndicatorColorDark
                        } else {
                            NotLiveIndicatorColorLight
                        }
                    },
                    shape = CircleShape,
                ),
        )
        if (!hideTextIfNotLive) {
            Text(
                text = stringResource(id = R.string.live_stream_live_indicator),
                color = textColor,
                style = AppTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                ),
            )
        }
    }
}

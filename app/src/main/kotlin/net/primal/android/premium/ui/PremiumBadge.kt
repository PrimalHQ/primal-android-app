package net.primal.android.premium.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun PremiumBadge(
    modifier: Modifier = Modifier,
    firstCohort: String,
    secondCohort: String,
    topColor: Color,
    middleColor: Color = topColor,
    bottomColor: Color = topColor,
) {
    val colorStops = arrayOf(
        0.0f to topColor,
        0.4937f to topColor,
        0.523f to middleColor,
        1f to bottomColor,
    )
    Row(
        modifier = modifier
            .clip(AppTheme.shapes.extraLarge)
            .background(
                brush = Brush.verticalGradient(
                    colorStops = colorStops,
                ),
            )
            .padding(start = 16.dp, end = 3.dp)
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = firstCohort,
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.bodyMedium,
        )
        Box(
            modifier = Modifier
                .clip(AppTheme.shapes.extraLarge)
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                text = secondCohort,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.primal.android.premium.legend.domain.LegendaryStyle
import net.primal.android.theme.AppTheme

@Composable
fun PremiumBadge(
    modifier: Modifier = Modifier,
    firstCohort: String,
    secondCohort: String,
    legendaryStyle: LegendaryStyle,
    membershipExpired: Boolean,
) {
    Row(
        modifier = modifier
            .clip(AppTheme.shapes.extraLarge)
            .background(
                brush = if (legendaryStyle != LegendaryStyle.NO_CUSTOMIZATION) {
                    legendaryStyle.primaryBrush
                } else {
                    Brush.linearGradient(listOf(AppTheme.colorScheme.tertiary, AppTheme.colorScheme.tertiary))
                },
                alpha = if (membershipExpired) 0.5f else 1.0f,
            )
            .padding(start = 16.dp, end = 3.dp)
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.padding(top = 1.5.dp),
            text = firstCohort,
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.bodyMedium,
            color = Color.White,
        )
        Box(
            modifier = Modifier
                .clip(AppTheme.shapes.extraLarge)
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .padding(top = 0.5.dp),
                text = secondCohort,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                color = Color.White,
            )
        }
    }
}

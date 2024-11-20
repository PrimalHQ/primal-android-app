package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun PremiumFeedPaywall(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt3,
                shape = AppTheme.shapes.extraLarge,
            )
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 4.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.feed_paywall_title),
            style = AppTheme.typography.bodyLarge,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.feed_paywall_description),
            style = AppTheme.typography.bodyMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(onClick = onClick) {
            Text(
                text = stringResource(R.string.feed_paywall_get_premium_button),
                color = AppTheme.colorScheme.secondary,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

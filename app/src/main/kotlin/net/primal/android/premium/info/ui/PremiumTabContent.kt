package net.primal.android.premium.info.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun PremiumTabContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        Text(
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            style = AppTheme.typography.bodyLarge,
            lineHeight = 24.sp,
            fontSize = 18.sp,
            textAlign = TextAlign.Justify,
            text = stringResource(id = R.string.premium_more_info_why_premium),
        )
    }
}

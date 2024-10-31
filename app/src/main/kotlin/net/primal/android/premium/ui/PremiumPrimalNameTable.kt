package net.primal.android.premium.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.theme.AppTheme


@Composable
fun PremiumPrimalNameTable(
    modifier: Modifier = Modifier,
    primalName: String,
) {
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.large)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3),
    ) {
        PrimalNameRow(
            key = stringResource(id = R.string.premium_primal_name_nostr_address),
            value = "$primalName@primal.net",
        )
        PrimalDivider()
        PrimalNameRow(
            key = stringResource(id = R.string.premium_primal_name_lightning_address),
            value = "$primalName@primal.net",
        )
        PrimalDivider()
        PrimalNameRow(
            key = stringResource(id = R.string.premium_primal_name_vip_profile),
            value = "primal.net/$primalName",
        )
    }
}

@Composable
private fun PrimalNameRow(
    modifier: Modifier = Modifier,
    key: String,
    value: String,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .padding(end = 8.dp)
                .weight(1f),
            text = key,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 16.sp,
        )
        Text(
            text = value,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 16.sp,
        )
    }
}

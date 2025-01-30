package net.primal.android.core.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun DailyBudgetPicker(
    modifier: Modifier = Modifier,
    dailyBudget: Long?,
    onChangeDailyBudgetBottomSheetVisibility: (bottomSheetVisible: Boolean) -> Unit,
) {
    ListItem(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .clickable { onChangeDailyBudgetBottomSheetVisibility(true) }
            .clip(AppTheme.shapes.small),
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        ),
        headlineContent = {
            Text(
                modifier = Modifier.padding(top = 3.dp),
                text = stringResource(id = R.string.settings_wallet_nwc_connections_header_daily_budget),
                style = AppTheme.typography.bodyLarge,
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (dailyBudget != null) {
                    Text(
                        modifier = Modifier.padding(top = 3.dp),
                        text = dailyBudget.toLong().let {
                            "%,d ${stringResource(id = R.string.wallet_sats_suffix)}".format(it)
                        },
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    )
                } else {
                    Text(
                        modifier = Modifier.padding(top = 3.dp),
                        text = stringResource(id = R.string.settings_wallet_nwc_connection_daily_budget_no_limit),
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    )
                }

                Spacer(modifier = Modifier.width(15.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
        },
    )
}

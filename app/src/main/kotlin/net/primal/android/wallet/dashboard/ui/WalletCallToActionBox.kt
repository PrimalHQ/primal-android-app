package net.primal.android.wallet.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.theme.AppTheme

@Composable
fun WalletCallToActionBox(
    modifier: Modifier,
    message: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.wrapContentSize(align = Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.padding(vertical = 16.dp),
                text = message,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                style = AppTheme.typography.bodyMedium,
            )

            if (actionLabel != null) {
                PrimalFilledButton(
                    modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                    onClick = { onActionClick?.invoke() },
                ) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}

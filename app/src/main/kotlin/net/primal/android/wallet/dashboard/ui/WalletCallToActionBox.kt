package net.primal.android.wallet.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.theme.AppTheme

@Composable
fun WalletCallToActionBox(
    modifier: Modifier,
    message: String? = null,
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
            message?.let {
                Text(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                    text = message,
                    textAlign = TextAlign.Center,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    style = AppTheme.typography.bodyMedium,
                )
            }

            if (actionLabel != null) {
                PrimalFilledButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onActionClick?.invoke() },
                ) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}

@Composable
fun WalletCallToActionAnnotatedBox(
    modifier: Modifier,
    message: AnnotatedString? = null,
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
            message?.let {
                Text(
                    modifier = Modifier.padding(vertical = 16.dp),
                    text = message,
                    textAlign = TextAlign.Center,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    style = AppTheme.typography.bodyMedium,
                )
            }

            if (actionLabel != null) {
                PrimalFilledButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onActionClick?.invoke() },
                ) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}

@Composable
fun WalletSetupCallToAction(
    modifier: Modifier,
    title: String,
    description: String,
    onRestoreWalletClick: () -> Unit,
    onCreateWalletClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = title,
            style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = AppTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = description,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        PrimalFilledButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onRestoreWalletClick,
        ) {
            Text(text = stringResource(id = R.string.wallet_dashboard_restore_button))
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onCreateWalletClick,
        ) {
            Text(
                text = stringResource(id = R.string.wallet_dashboard_create_new_button),
                color = AppTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold,
                style = AppTheme.typography.bodyLarge,
            )
        }
    }
}

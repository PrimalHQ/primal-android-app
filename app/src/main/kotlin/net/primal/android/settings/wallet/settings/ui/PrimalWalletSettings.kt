package net.primal.android.settings.wallet.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.settings.SettingsItem
import net.primal.android.settings.wallet.settings.WalletSettingsContract
import net.primal.android.settings.wallet.settings.WalletSettingsContract.UiEvent
import net.primal.android.theme.AppTheme
import net.primal.core.utils.CurrencyConversionUtils.toSats

@Composable
fun PrimalWalletSettings(state: WalletSettingsContract.UiState, eventPublisher: (UiEvent) -> Unit) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    Column {
//        Spacer(modifier = Modifier.height(8.dp))
//
//        SettingsItem(
//            headlineText = stringResource(id = R.string.settings_wallet_start_in_wallet),
//            supportText = stringResource(id = R.string.settings_wallet_start_in_wallet_hint),
//            trailingContent = {
//                PrimalSwitch(
//                    checked = false,
//                    onCheckedChange = {
//                    },
//                )
//            },
//            onClick = {
//            },
//        )

        Spacer(modifier = Modifier.height(8.dp))

        var spamThresholdAmountEditorDialog by remember { mutableStateOf(false) }
        val spamThresholdAmountInSats = state.spamThresholdAmountInSats?.let {
            numberFormat.format(it)
        } ?: "1"
        SettingsItem(
            headlineText = stringResource(
                id = R.string.settings_wallet_hide_transactions_below,
            ),
            supportText = "$spamThresholdAmountInSats sats",
            onClick = { spamThresholdAmountEditorDialog = true },
        )

        if (spamThresholdAmountEditorDialog) {
            SpamThresholdAmountEditorDialog(
                onDialogDismiss = { spamThresholdAmountEditorDialog = false },
                onEditAmount = {
                    eventPublisher(UiEvent.UpdateMinTransactionAmount(amountInSats = it))
                },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        var maxWalletBalanceShown by remember { mutableStateOf(false) }
        val maxBalanceInSats =
            numberFormat.format((state.maxWalletBalanceInBtc ?: "0.01").toSats().toLong())
        SettingsItem(
            headlineText = stringResource(id = R.string.settings_wallet_max_wallet_balance),
            supportText = "$maxBalanceInSats sats",
            trailingContent = {
                IconButton(onClick = { maxWalletBalanceShown = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(
                            id = R.string.accessibility_info,
                        ),
                    )
                }
            },
            onClick = { maxWalletBalanceShown = true },
        )

        if (maxWalletBalanceShown) {
            MaxWalletBalanceDialog(
                text = stringResource(id = R.string.settings_wallet_max_wallet_balance_hint),
                onDialogDismiss = { maxWalletBalanceShown = false },
            )
        }
    }
}

@Composable
private fun SpamThresholdAmountEditorDialog(onDialogDismiss: () -> Unit, onEditAmount: (Long) -> Unit) {
    var amount by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDialogDismiss,
        title = { Text(text = stringResource(id = R.string.settings_wallet_hide_transactions_below_edit_title)) },
        text = {
            OutlinedTextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = amount,
                onValueChange = {
                    when {
                        it.isEmpty() -> amount = ""
                        it.isDigitsOnly() && it.length <= 6 && it.toLong() > 0 -> amount = it
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onEditAmount(amount.toLongOrNull() ?: 1L)
                        onDialogDismiss()
                    },
                ),
            )
        },
        dismissButton = {
            TextButton(onClick = onDialogDismiss) {
                Text(
                    text = stringResource(id = android.R.string.cancel),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onEditAmount(amount.toLongOrNull() ?: 1L)
                    onDialogDismiss()
                },
            ) {
                Text(
                    text = stringResource(id = R.string.settings_wallet_save),
                )
            }
        },
    )
}

@Composable
private fun MaxWalletBalanceDialog(text: String, onDialogDismiss: () -> Unit) {
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDialogDismiss,
        title = { Text(text = stringResource(id = R.string.app_info)) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(onClick = onDialogDismiss) {
                Text(
                    text = stringResource(id = android.R.string.ok),
                )
            }
        },
    )
}

package net.primal.android.settings.wallet.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.*
import net.primal.android.R
import net.primal.android.core.compose.ConfirmActionAlertDialog
import net.primal.android.core.compose.DeleteListItemImage
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.settings.wallet.domain.NwcConnectionInfo
import net.primal.android.settings.wallet.settings.WalletSettingsContract
import net.primal.android.theme.AppTheme
import net.primal.core.utils.CurrencyConversionUtils.toSats

@Composable
fun ConnectedAppsSettings(
    nwcConnectionInfos: List<NwcConnectionInfo>,
    isPrimalWalletActivated: Boolean,
    connectionsState: WalletSettingsContract.ConnectionsState,
    onRevokeConnectedApp: (nwcPubkey: String) -> Unit,
    onCreateNewWalletConnection: () -> Unit,
    onRetryFetchingConnections: () -> Unit,
) {
    var revokeDialogVisible by remember { mutableStateOf(false) }
    var revokeNwcPubkey by remember { mutableStateOf("") }

    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        headlineContent = {
            Text(
                text = stringResource(
                    id = R.string.settings_wallet_nwc_connections_connected_apps,
                ).uppercase(Locale.getDefault()),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold,
            )
        },
    )

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = RoundedCornerShape(8.dp),
            ),
    ) {
        ConnectedAppsHeader()

        HorizontalDivider(thickness = 1.dp)

        ConnectedAppsContent(
            connectionsState = connectionsState,
            nwcConnectionInfos = nwcConnectionInfos,
            onRetryFetchingConnections = onRetryFetchingConnections,
            onRevokeDialogVisibilityChange = { revokeDialogVisible = it },
            onRevokeNwcPubkeyChange = { revokeNwcPubkey = it },
            isPrimalWalletActivated = isPrimalWalletActivated,
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    ConnectedAppsHint(
        isPrimalWalletActivated = isPrimalWalletActivated,
        createNewWalletConnection = onCreateNewWalletConnection,
    )

    if (revokeDialogVisible) {
        ConfirmActionAlertDialog(
            confirmText = stringResource(id = R.string.feed_list_dialog_confirm),
            dismissText = stringResource(id = R.string.feed_list_dialog_dismiss),
            onDismissRequest = {
                revokeDialogVisible = false
                revokeNwcPubkey = ""
            },
            onConfirmation = {
                revokeDialogVisible = false
                onRevokeConnectedApp(revokeNwcPubkey)
            },
            dialogTitle = stringResource(id = R.string.settings_wallet_nwc_connections_revoke_connection_dialog_title),
            dialogText = stringResource(id = R.string.settings_wallet_nwc_connections_revoke_connection_dialog_text),
        )
    }
}

@Composable
private fun ConnectedAppsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.settings_wallet_nwc_connections_header_app),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
        )
        Text(
            text = stringResource(id = R.string.settings_wallet_nwc_connections_header_daily_budget),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(WEIGHT_DAILY_BUDGET),
            textAlign = TextAlign.Start,
        )
        Text(
            text = stringResource(id = R.string.settings_wallet_nwc_connections_header_revoke),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun ConnectedAppsContent(
    connectionsState: WalletSettingsContract.ConnectionsState,
    isPrimalWalletActivated: Boolean,
    onRetryFetchingConnections: () -> Unit,
    nwcConnectionInfos: List<NwcConnectionInfo>,
    onRevokeDialogVisibilityChange: (Boolean) -> Unit,
    onRevokeNwcPubkeyChange: (String) -> Unit,
) {
    when (connectionsState) {
        WalletSettingsContract.ConnectionsState.Loading -> {
            Box(modifier = Modifier.height(48.dp)) {
                PrimalLoadingSpinner(size = 32.dp)
            }
        }

        WalletSettingsContract.ConnectionsState.Error -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = when (isPrimalWalletActivated) {
                        true -> stringResource(R.string.settings_wallet_nwc_connections_error_unable_to_load_apps)
                        else -> stringResource(R.string.settings_wallet_nwc_connections_wallet_not_activated)
                    },
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    fontWeight = FontWeight.Medium,
                )

                if (isPrimalWalletActivated) {
                    TextButton(onClick = onRetryFetchingConnections) {
                        Text(
                            text = stringResource(id = R.string.settings_wallet_nwc_connections_retry),
                        )
                    }
                }
            }
        }

        WalletSettingsContract.ConnectionsState.Loaded -> {
            if (nwcConnectionInfos.isEmpty()) {
                Box(modifier = Modifier.height(48.dp)) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .align(Alignment.Center),
                        text = stringResource(id = R.string.settings_wallet_nwc_connections_no_connected_apps),
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                nwcConnectionInfos.forEachIndexed { index, app ->
                    val isLastItem = index == nwcConnectionInfos.lastIndex

                    ConnectedAppItem(
                        isLastItem = isLastItem,
                        appName = app.appName,
                        budget = if (app.dailyBudget?.isNotBlank() == true) {
                            app.dailyBudget.toSats().toLong().let { "%,d sats".format(it) }
                        } else {
                            stringResource(id = R.string.settings_wallet_nwc_connection_daily_budget_no_limit)
                        },
                        canRevoke = app.canRevoke,
                        onRevokeConnectedApp = {
                            onRevokeDialogVisibilityChange(true)
                            onRevokeNwcPubkeyChange(app.nwcPubkey)
                        },
                    )

                    if (!isLastItem) {
                        HorizontalDivider(thickness = 1.dp)
                    }
                }
            }
        }
    }
}

private const val WEIGHT_DAILY_BUDGET = 1.1f

@Composable
private fun ConnectedAppItem(
    isLastItem: Boolean,
    appName: String,
    budget: String,
    canRevoke: Boolean,
    onRevokeConnectedApp: () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt3,
                shape = if (isLastItem) {
                    RoundedCornerShape(
                        bottomStart = 8.dp,
                        bottomEnd = 8.dp,
                    )
                } else {
                    RoundedCornerShape(0.dp)
                },
            )
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = appName,
            maxLines = 1,
            textAlign = TextAlign.Start,
        )
        Text(
            modifier = Modifier.weight(WEIGHT_DAILY_BUDGET),
            text = budget,
            maxLines = 1,
            textAlign = TextAlign.Start,
        )

        if (canRevoke) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(
                    modifier = Modifier.offset(x = 7.dp),
                    onClick = onRevokeConnectedApp,
                ) {
                    DeleteListItemImage()
                }
            }
        }
    }
}

@Composable
private fun ConnectedAppsHint(isPrimalWalletActivated: Boolean, createNewWalletConnection: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.settings_wallet_nwc_connections_hint),
            style = AppTheme.typography.bodySmall,
        )

        if (isPrimalWalletActivated) {
            TextButton(
                onClick = createNewWalletConnection,
                contentPadding = PaddingValues(0.dp),
                shape = AppTheme.shapes.small,
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = createNewWalletConnection,
                        ),
                    text = stringResource(id = R.string.settings_wallet_nwc_connections_create_new_text_button),
                    style = AppTheme.typography.bodyMedium.copy(
                        color = AppTheme.colorScheme.secondary,
                        fontStyle = AppTheme.typography.bodyMedium.fontStyle,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }
    }
}

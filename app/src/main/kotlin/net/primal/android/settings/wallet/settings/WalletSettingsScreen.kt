package net.primal.android.settings.wallet.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import net.primal.android.R
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.DownloadsFilled
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.compose.settings.SettingsItem
import net.primal.android.settings.wallet.settings.WalletSettingsContract.UiEvent
import net.primal.android.settings.wallet.settings.ui.ConnectedAppsSettings
import net.primal.android.settings.wallet.settings.ui.ExternalWalletSettings
import net.primal.android.settings.wallet.settings.ui.PrimalWalletSettings
import net.primal.android.settings.wallet.settings.ui.WalletBackupWidget
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.domain.utils.isPrimalWalletAndActivated
import net.primal.domain.wallet.NostrWalletKeypair
import net.primal.domain.wallet.Wallet

@Composable
fun WalletSettingsScreen(
    viewModel: WalletSettingsViewModel,
    onClose: () -> Unit,
    onEditProfileClick: () -> Unit,
    onScanNwcClick: () -> Unit,
    onCreateNewWalletConnection: () -> Unit,
    onRestoreWalletClick: () -> Unit,
    onBackupWalletClick: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()
    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> viewModel.setEvent(UiEvent.RequestFetchWalletConnections)
            else -> Unit
        }
    }

    WalletSettingsScreen(
        state = uiState.value,
        onClose = onClose,
        onEditProfileClick = onEditProfileClick,
        onScanNwcClick = onScanNwcClick,
        onCreateNewWalletConnection = onCreateNewWalletConnection,
        onRestoreWalletClick = onRestoreWalletClick,
        onBackupWalletClick = onBackupWalletClick,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSettingsScreen(
    state: WalletSettingsContract.UiState,
    onClose: () -> Unit,
    onEditProfileClick: () -> Unit,
    onScanNwcClick: () -> Unit,
    onCreateNewWalletConnection: () -> Unit,
    onRestoreWalletClick: () -> Unit,
    onBackupWalletClick: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    val scrollState = rememberScrollState()

    PrimalScaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_wallet_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .background(color = AppTheme.colorScheme.surfaceVariant)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (state.showBackupWidget) {
                    Spacer(modifier = Modifier.height(16.dp))
                    WalletBackupWidget(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        walletBalanceInBtc = state.wallet?.balanceInBtc?.toString(),
                        onBackupClick = onBackupWalletClick,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                NostrProfileLightingAddressSection(
                    lightningAddress = state.wallet?.lightningAddress,
                    onEditProfileClick = onEditProfileClick,
                )

                Spacer(modifier = Modifier.height(8.dp))

                WalletSpecificSettingsItems(
                    state = state,
                    eventPublisher = eventPublisher,
                    onScanNwcClick = onScanNwcClick,
                    onBackupWalletClick = onBackupWalletClick,
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_wallet_restore_wallet_title),
                    supportText = stringResource(id = R.string.settings_wallet_restore_wallet_subtitle),
                    trailingContent = {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null)
                    },
                    onClick = onRestoreWalletClick,
                )
                Spacer(modifier = Modifier.height(8.dp))

                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_wallet_export_transactions_title),
                    supportText = stringResource(id = R.string.settings_wallet_export_transactions_subtitle),
                    trailingContent = {
                        Icon(imageVector = PrimalIcons.DownloadsFilled, contentDescription = null)
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExternalWalletListItem(
                    useExternalWallet = state.useExternalWallet == true,
                    onExternalWalletSwitchChanged = { value ->
                        eventPublisher(UiEvent.UpdateUseExternalWallet(value))
                    },
                )

                ConnectedAppsSettings(
                    primalNwcConnectionInfos = state.nwcConnectionsInfo,
                    onRevokeConnectedApp = { eventPublisher(UiEvent.RevokeConnection(it)) },
                    onCreateNewWalletConnection = onCreateNewWalletConnection,
                    connectionsState = state.connectionsState,
                    onRetryFetchingConnections = { eventPublisher(UiEvent.RequestFetchWalletConnections) },
                    isPrimalWalletActivated = state.wallet?.isPrimalWalletAndActivated() == true,
                )
            }
        },
    )
}

@Composable
private fun WalletSpecificSettingsItems(
    state: WalletSettingsContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    onScanNwcClick: () -> Unit,
    onBackupWalletClick: () -> Unit,
) {
    AnimatedContent(targetState = state.useExternalWallet == true, label = "WalletSettingsContent") {
        when (it) {
            true -> {
                val clipboardManager = LocalClipboardManager.current
                ExternalWalletSettings(
                    nwcWallet = state.wallet,
                    onExternalWalletDisconnect = { eventPublisher(UiEvent.DisconnectWallet) },
                    onPasteNwcClick = {
                        val clipboardText = clipboardManager.getText()?.text.orEmpty().trim()
                        eventPublisher(UiEvent.ConnectExternalWallet(connectionLink = clipboardText))
                    },
                    onScanNwcClick = onScanNwcClick,
                )
            }

            false -> {
                PrimalWalletSettings(
                    state = state,
                    eventPublisher = eventPublisher,
                    onBackupWalletClick = onBackupWalletClick,
                )
            }
        }
    }
}

@Composable
private fun ExternalWalletListItem(useExternalWallet: Boolean, onExternalWalletSwitchChanged: (Boolean) -> Unit) {
    ListItem(
        modifier = Modifier.clickable {
            onExternalWalletSwitchChanged(!useExternalWallet)
        },
        headlineContent = {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = stringResource(id = R.string.settings_wallet_use_external_wallet),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colorScheme.onPrimary,
            )
        },
        supportingContent = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.settings_wallet_use_external_wallet_hint),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        },
        trailingContent = {
            PrimalSwitch(
                checked = useExternalWallet,
                onCheckedChange = onExternalWalletSwitchChanged,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
    )
}

@Composable
private fun NostrProfileLightingAddressSection(lightningAddress: String?, onEditProfileClick: () -> Unit) {
    Column {
        SettingsItem(
            headlineText = stringResource(id = R.string.settings_wallet_ln_address),
            supportText = lightningAddress,
            trailingContent = {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null)
            },
            onClick = onEditProfileClick,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onEditProfileClick,
                    ),
                text = buildAnnotatedString {
                    append(stringResource(id = R.string.settings_wallet_nwc_profile_lightning_address_hint))
                    append(
                        AnnotatedString(
                            text = " ${
                                stringResource(id = R.string.settings_wallet_nwc_profile_lightning_address_hint_suffix)
                            }",
                            spanStyle = SpanStyle(
                                color = AppTheme.colorScheme.secondary,
                                fontStyle = AppTheme.typography.bodySmall.fontStyle,
                            ),
                        ),
                    )
                    append(".")
                },
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

class WalletUiStateProvider : PreviewParameterProvider<WalletSettingsContract.UiState> {
    override val values: Sequence<WalletSettingsContract.UiState>
        get() = sequenceOf(
            WalletSettingsContract.UiState(
                wallet = null,
            ),
            WalletSettingsContract.UiState(
                wallet = null,
            ),
            WalletSettingsContract.UiState(
                wallet = Wallet.NWC(
                    relays = listOf("wss://relay.getalby.com/v1"),
                    lightningAddress = "miljan@getalby.com",
                    walletId = "69effe7b49a6dd5cf525bd0905917a5005ffe480b58eeb8e861418cf3ae760d9",
                    userId = "someUserId",
                    spamThresholdAmountInSats = 1L,
                    balanceInBtc = 0.0,
                    maxBalanceInBtc = 0.0,
                    lastUpdatedAt = null,
                    pubkey = "somePubkey",
                    keypair = NostrWalletKeypair(
                        privateKey = "7c0dabd065b2de3299a0d0e1c26b8ac7047dae6b20aba3a62b23650eb601bbfd",
                        pubkey = "69effe7b49a6dd5cf525bd0905917a5005ffe480b58eeb8e861418cf3ae760d9",
                    ),
                ),
            ),
        )
}

@Preview
@Composable
private fun PreviewSettingsWalletScreen(
    @PreviewParameter(WalletUiStateProvider::class)
    state: WalletSettingsContract.UiState,
) {
    PrimalPreview(primalTheme = PrimalTheme.Sunrise) {
        WalletSettingsScreen(
            state = state,
            onClose = {},
            onEditProfileClick = {},
            onScanNwcClick = {},
            onCreateNewWalletConnection = {},
            eventPublisher = {},
            onRestoreWalletClick = {},
            onBackupWalletClick = {},
        )
    }
}

package net.primal.android.settings.wallet.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
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
import net.primal.android.core.service.PRIMAL_SERVICE_NOTIFICATION_CHANNEL_ID
import net.primal.android.core.service.PrimalNwcService
import net.primal.android.core.utils.hasNotificationPermission
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.settings.wallet.settings.WalletSettingsContract.UiEvent
import net.primal.android.settings.wallet.settings.ui.ConnectedAppsSettings
import net.primal.android.settings.wallet.settings.ui.EnableNwcNotificationsBottomSheet
import net.primal.android.settings.wallet.settings.ui.ExternalWalletSettings
import net.primal.android.settings.wallet.settings.ui.PrimalWalletSettings
import net.primal.android.settings.wallet.settings.ui.WalletBackupWidget
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.wallet.utils.saveNwcLogsToUri
import net.primal.android.wallet.utils.saveTransactionsToUri
import net.primal.domain.links.CdnImage
import net.primal.domain.wallet.NostrWalletKeypair
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.capabilities

@Composable
fun WalletSettingsScreen(
    viewModel: WalletSettingsViewModel,
    onClose: () -> Unit,
    onEditProfileClick: () -> Unit,
    onScanNwcClick: () -> Unit,
    onCreateNewWalletConnection: () -> Unit,
    onRestoreWalletClick: () -> Unit,
    onBackupWalletClick: (String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()
    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> viewModel.setEvent(UiEvent.RequestFetchWalletConnections)
            else -> Unit
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        val transactions = uiState.value.transactionsToExport
        if (uri != null && transactions.isNotEmpty()) {
            scope.launch {
                saveTransactionsToUri(context, uri, transactions)
            }
        }
    }

    val saveNwcLogsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        val logs = uiState.value.nwcLogsToExport
        if (uri != null && logs.isNotEmpty()) {
            scope.launch {
                saveNwcLogsToUri(context, uri, logs)
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                WalletSettingsContract.SideEffect.TransactionsReadyForExport -> {
                    val fileName = "${uiState.value.activeWallet?.type}_transactions.csv"
                    saveFileLauncher.launch(fileName)
                }
                WalletSettingsContract.SideEffect.NwcLogsReadyForExport -> {
                    saveNwcLogsLauncher.launch("nwc_audit_logs.csv")
                }
            }
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

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSettingsScreen(
    state: WalletSettingsContract.UiState,
    onClose: () -> Unit,
    onEditProfileClick: () -> Unit,
    onScanNwcClick: () -> Unit,
    onCreateNewWalletConnection: () -> Unit,
    onRestoreWalletClick: () -> Unit,
    onBackupWalletClick: (String) -> Unit,
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
                        walletBalanceInBtc = state.activeWallet?.balanceInBtc?.toString(),
                        onBackupClick = {
                            state.activeWallet?.walletId?.let(onBackupWalletClick)
                        },
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                NostrProfileLightingAddressSection(
                    lightningAddress = state.activeWallet?.lightningAddress,
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

                if (state.activeWallet is Wallet.Spark) {
                    SettingsItem(
                        headlineText = stringResource(id = R.string.settings_wallet_restore_wallet_title),
                        supportText = stringResource(id = R.string.settings_wallet_restore_wallet_subtitle),
                        trailingContent = {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null)
                        },
                        onClick = onRestoreWalletClick,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (state.showRevertToPrimalWallet) {
                    SettingsItem(
                        headlineText = stringResource(id = R.string.settings_wallet_revert_to_primal_title),
                        supportText = stringResource(id = R.string.settings_wallet_revert_to_primal_subtitle),
                        enabled = !state.isRevertingToPrimalWallet,
                        trailingContent = {
                            if (state.isRevertingToPrimalWallet) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                )
                            }
                        },
                        onClick = { eventPublisher(UiEvent.RevertToPrimalWallet) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_wallet_export_transactions_title),
                    supportText = stringResource(id = R.string.settings_wallet_export_transactions_subtitle),
                    enabled = !state.isExportingTransactions,
                    trailingContent = {
                        if (state.isExportingTransactions) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(imageVector = PrimalIcons.DownloadsFilled, contentDescription = null)
                        }
                    },
                    onClick = { eventPublisher(UiEvent.RequestTransactionExport) },
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExternalWalletListItem(
                    useExternalWallet = state.useExternalWallet == true,
                    onExternalWalletSwitchChanged = { value ->
                        eventPublisher(UiEvent.UpdateUseExternalWallet(value))
                    },
                )

                if (state.activeWallet?.capabilities?.supportsNwcConnections == true) {
                    ConnectedAppsSettings(
                        primalNwcConnectionInfos = state.nwcConnectionsInfo,
                        onRevokeConnectedApp = { eventPublisher(UiEvent.RevokeConnection(it)) },
                        onCreateNewWalletConnection = onCreateNewWalletConnection,
                        connectionsState = state.connectionsState,
                        onRetryFetchingConnections = { eventPublisher(UiEvent.RequestFetchWalletConnections) },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_wallet_export_nwc_logs_title),
                    supportText = stringResource(id = R.string.settings_wallet_export_nwc_logs_subtitle),
                    enabled = !state.isExportingNwcLogs,
                    trailingContent = {
                        if (state.isExportingNwcLogs) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(imageVector = PrimalIcons.DownloadsFilled, contentDescription = null)
                        }
                    },
                    onClick = { eventPublisher(UiEvent.RequestNwcLogsExport) },
                )

                ToggleNwcServiceButton(
                    currentUserId = state.activeUserId,
                    isRunningForCurrentUser = state.isServiceRunningForCurrentUser,
                    avatarCdnImage = state.activeAccountAvatarCdnImage,
                    legendaryCustomization = state.activeAccountLegendaryCustomization,
                    avatarBlossoms = state.activeAccountBlossoms,
                    displayName = state.activeAccountDisplayName,
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        },
    )
}

@Composable
private fun ToggleNwcServiceButton(
    currentUserId: String,
    isRunningForCurrentUser: Boolean,
    avatarCdnImage: CdnImage?,
    legendaryCustomization: LegendaryCustomization?,
    avatarBlossoms: List<String>,
    displayName: String,
) {
    val context = LocalContext.current
    var showNotificationsBottomSheet by remember { mutableStateOf(false) }

    if (showNotificationsBottomSheet) {
        EnableNwcNotificationsBottomSheet(
            avatarCdnImage = avatarCdnImage,
            legendaryCustomization = legendaryCustomization,
            avatarBlossoms = avatarBlossoms,
            displayName = displayName,
            onDismissRequest = { showNotificationsBottomSheet = false },
            onTogglePushNotifications = { enabled ->
                if (enabled) {
                    PrimalNwcService.start(context, currentUserId)
                    showNotificationsBottomSheet = false
                }
            },
        )
    }

    TextButton(
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = {
            if (isRunningForCurrentUser) {
                PrimalNwcService.stop(context, currentUserId)
            } else {
                if (context.hasNotificationPermission(PRIMAL_SERVICE_NOTIFICATION_CHANNEL_ID)) {
                    PrimalNwcService.start(context, currentUserId)
                } else {
                    showNotificationsBottomSheet = true
                }
            }
        },
        contentPadding = PaddingValues(0.dp),
        shape = AppTheme.shapes.small,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(
                id = if (isRunningForCurrentUser) {
                    R.string.settings_wallet_nwc_service_stop
                } else {
                    R.string.settings_wallet_nwc_service_start
                },
            ),
            style = AppTheme.typography.bodyMedium.copy(
                color = AppTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Composable
private fun WalletSpecificSettingsItems(
    state: WalletSettingsContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    onScanNwcClick: () -> Unit,
    onBackupWalletClick: (String) -> Unit,
) {
    AnimatedContent(targetState = state.useExternalWallet == true, label = "WalletSettingsContent") {
        when (it) {
            true -> {
                val clipboardManager = LocalClipboardManager.current
                ExternalWalletSettings(
                    nwcWallet = state.activeWallet,
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
                activeUserId = "",
                activeWallet = null,
            ),
            WalletSettingsContract.UiState(
                activeUserId = "",
                activeWallet = null,
            ),
            WalletSettingsContract.UiState(
                activeUserId = "",
                activeWallet = Wallet.NWC(
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
            onBackupWalletClick = { _ -> },
        )
    }
}

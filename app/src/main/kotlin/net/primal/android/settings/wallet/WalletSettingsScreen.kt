package net.primal.android.settings.wallet

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.settings.SettingsItem
import net.primal.android.settings.wallet.WalletSettingsContract.UiEvent
import net.primal.android.settings.wallet.model.ConnectedAppUi
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.user.domain.NostrWalletKeypair
import net.primal.android.user.domain.WalletPreference
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats

private val errorColor = Color(0xFFFE3D2F)

@Composable
fun WalletSettingsScreen(
    viewModel: WalletSettingsViewModel,
    onClose: () -> Unit,
    onEditProfileClick: () -> Unit,
    onOtherConnectClick: () -> Unit,
    onCreateNewWalletConnection: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    WalletSettingsScreen(
        state = uiState.value,
        onClose = onClose,
        onEditProfileClick = onEditProfileClick,
        onOtherConnectClick = onOtherConnectClick,
        onCreateNewWalletConnection = onCreateNewWalletConnection,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSettingsScreen(
    state: WalletSettingsContract.UiState,
    onClose: () -> Unit,
    onEditProfileClick: () -> Unit,
    onOtherConnectClick: () -> Unit,
    onCreateNewWalletConnection: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    val scrollState = rememberScrollState()
    val primalWalletPreferred = state.walletPreference != WalletPreference.NostrWalletConnect

    Scaffold(
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
                Spacer(modifier = Modifier.height(16.dp))

                ExternalWalletListItem(
                    preferPrimalWallet = primalWalletPreferred,
                    onExternalWalletSwitchChanged = { enabled ->
                        eventPublisher(
                            UiEvent.UpdateWalletPreference(
                                walletPreference = if (enabled) {
                                    WalletPreference.PrimalWallet
                                } else {
                                    WalletPreference.NostrWalletConnect
                                },
                            ),
                        )
                    },
                )

                AnimatedContent(targetState = primalWalletPreferred, label = "WalletSettingsContent") {
                    when (it) {
                        true -> {
                            PrimalWalletSettings(
                                state = state,
                                eventPublisher = eventPublisher,
                            )
                        }

                        false -> {
                            ExternalWalletSettings(
                                nwcWallet = state.wallet,
                                onExternalWalletDisconnect = {
                                    eventPublisher(UiEvent.DisconnectWallet)
                                },
                                onOtherConnectClick = onOtherConnectClick,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                NostrProfileLightingAddressSection(
                    lightningAddress = state.userLightningAddress,
                    onEditProfileClick = onEditProfileClick,
                )

                Spacer(modifier = Modifier.height(16.dp))

                ConnectedAppsSettings(
                    connectedAppUis = state.connectedApps,
                    onRevokeConnectedApp = {},
                    onCreateNewWalletConnection = onCreateNewWalletConnection,
                )
            }
        },
    )
}

@Composable
private fun ConnectedAppsSettings(
    connectedAppUis: List<ConnectedAppUi>,
    onRevokeConnectedApp: () -> Unit,
    onCreateNewWalletConnection: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(id = R.string.settings_wallet_connected_apps),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Left,
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

        if (connectedAppUis.isEmpty()) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 21.dp)
                    .align(Alignment.CenterHorizontally),
                text = stringResource(id = R.string.settings_wallet_no_connected_apps),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
        } else {
            connectedAppUis.forEachIndexed { index, app ->
                val isLastItem = index == connectedAppUis.lastIndex

                ConnectedAppItem(
                    isLastItem = isLastItem,
                    appName = app.appName,
                    budget = app.dailyBudget,
                    canRevoke = app.canRevoke,
                    onRevokeConnectedApp = onRevokeConnectedApp,
                )

                if (!isLastItem) {
                    HorizontalDivider(thickness = 1.dp)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    ConnectedAppsHint(
        createNewWalletConnection = onCreateNewWalletConnection,
    )
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
            text = stringResource(id = R.string.settings_wallet_header_app),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Left,
        )
        Text(
            text = stringResource(id = R.string.settings_wallet_header_daily_budget),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(id = R.string.settings_wallet_header_revoke),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Right,
        )
    }
}

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
            text =
            if (appName.length > 10) {
                "${appName.take(10)}..."
            } else {
                appName
            },
            modifier = Modifier.weight(1f),
        )
        Text(text = budget, modifier = Modifier.weight(1f))

        if (canRevoke) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(
                    modifier = Modifier.offset(x = 7.dp),
                    onClick = onRevokeConnectedApp,
                ) {
                    Icon(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(color = errorColor),
                        imageVector = Icons.Default.Remove,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectedAppsHint(createNewWalletConnection: () -> Unit) {
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
                    onClick = createNewWalletConnection,
                ),
            text = buildAnnotatedString {
                append(stringResource(id = R.string.settings_wallet_connected_apps_hint))
                append(
                    AnnotatedString(
                        text = stringResource(id = R.string.settings_wallet_connected_apps_hint_button),
                        spanStyle = SpanStyle(
                            color = AppTheme.colorScheme.secondary,
                            fontStyle = AppTheme.typography.bodySmall.fontStyle,
                            fontWeight = FontWeight.SemiBold,

                        ),
                    ),
                )
            },
            style = AppTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun PrimalWalletSettings(state: WalletSettingsContract.UiState, eventPublisher: (UiEvent) -> Unit) {
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
private fun ExternalWalletListItem(preferPrimalWallet: Boolean, onExternalWalletSwitchChanged: (Boolean) -> Unit) {
    ListItem(
        modifier = Modifier.clickable {
            onExternalWalletSwitchChanged(!preferPrimalWallet)
        },
        headlineContent = {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = stringResource(id = R.string.settings_wallet_use_primal_wallet),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colorScheme.onPrimary,
            )
        },
        supportingContent = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.settings_wallet_use_primal_wallet_hint),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        },
        trailingContent = {
            PrimalSwitch(
                checked = preferPrimalWallet,
                onCheckedChange = onExternalWalletSwitchChanged,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
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
                walletPreference = WalletPreference.PrimalWallet,
                userLightningAddress = "alex@primal.net",
            ),
            WalletSettingsContract.UiState(
                wallet = null,
                walletPreference = WalletPreference.NostrWalletConnect,
                userLightningAddress = "alex@primal.net",
            ),
            WalletSettingsContract.UiState(
                wallet = NostrWalletConnect(
                    relays = listOf("wss://relay.getalby.com/v1"),
                    lightningAddress = "miljan@getalby.com",
                    pubkey = "69effe7b49a6dd5cf525bd0905917a5005ffe480b58eeb8e861418cf3ae760d9",
                    keypair = NostrWalletKeypair(
                        privateKey = "7c0dabd065b2de3299a0d0e1c26b8ac7047dae6b20aba3a62b23650eb601bbfd",
                        pubkey = "69effe7b49a6dd5cf525bd0905917a5005ffe480b58eeb8e861418cf3ae760d9",
                    ),
                ),
                walletPreference = WalletPreference.NostrWalletConnect,
                userLightningAddress = "alex@primal.net",
            ),
        )
}

@Preview
@Composable
private fun PreviewSettingsWalletScreen(
    @PreviewParameter(WalletUiStateProvider::class)
    state: WalletSettingsContract.UiState,
) {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        WalletSettingsScreen(
            state = state,
            onClose = {},
            onEditProfileClick = {},
            onOtherConnectClick = {},
            onCreateNewWalletConnection = {},
            eventPublisher = {},
        )
    }
}

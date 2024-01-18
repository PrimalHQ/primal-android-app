package net.primal.android.settings.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.settings.wallet.WalletSettingsContract.UiEvent
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.user.domain.NostrWalletKeypair
import net.primal.android.user.domain.WalletPreference
import timber.log.Timber

@Composable
fun WalletSettingsScreen(
    viewModel: WalletSettingsViewModel,
    onClose: () -> Unit,
    onEditProfileClick: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    WalletSettingsScreen(
        state = uiState.value,
        onClose = onClose,
        onEditProfileClick = onEditProfileClick,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSettingsScreen(
    state: WalletSettingsContract.UiState,
    onClose: () -> Unit,
    onEditProfileClick: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    val scrollState = rememberScrollState()
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_wallet_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ExternalWalletSettings(
                    nwcWallet = state.wallet,
                    walletPreference = state.walletPreference,
                    profileLightningAddress = state.userLightningAddress,
                    onExternalWalletSwitchChanged = { enabled ->
                        Timber.e("onExternalWalletSwitchChanged = $enabled")
                        eventPublisher(
                            UiEvent.UpdateWalletPreference(
                                walletPreference = if (enabled) {
                                    WalletPreference.NostrWalletConnect
                                } else {
                                    WalletPreference.PrimalWallet
                                },
                            ),
                        )
                    },
                    onExternalWalletDisconnect = {
                        eventPublisher(UiEvent.DisconnectWallet)
                    },
                    onEditProfileClick = onEditProfileClick,
                )
            }
        },
    )
}

class WalletUiStateProvider : PreviewParameterProvider<WalletSettingsContract.UiState> {
    override val values: Sequence<WalletSettingsContract.UiState>
        get() = sequenceOf(
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
            WalletSettingsContract.UiState(
                wallet = null,
                walletPreference = WalletPreference.NostrWalletConnect,
                userLightningAddress = "alex@primal.net",
            ),
            WalletSettingsContract.UiState(
                wallet = null,
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
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        WalletSettingsScreen(
            state = state,
            onClose = {},
            onEditProfileClick = {},
            eventPublisher = {},
        )
    }
}

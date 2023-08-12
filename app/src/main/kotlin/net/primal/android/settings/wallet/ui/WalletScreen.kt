package net.primal.android.settings.wallet.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.nostr.model.HexVariant
import net.primal.android.settings.wallet.WalletContract
import net.primal.android.settings.wallet.WalletViewModel
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.user.domain.NostrWalletConnect

@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
    onClose: () -> Unit
) {
    val uiState = viewModel.state.collectAsState()

    val disconnectWallet = {
        viewModel.setEvent(WalletContract.UiEvent.DisconnectWallet)
    }

    WalletScreen(
        state = uiState.value,
        onClose = onClose,
        disconnectWallet = disconnectWallet
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    state: WalletContract.UiState,
    onClose: () -> Unit,
    disconnectWallet: () -> Unit
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title =
                if (state.isWalletConnected)
                    stringResource(id = R.string.settings_wallet_connected_title)
                else
                    stringResource(id = R.string.settings_wallet_not_connected_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(top = 50.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.isWalletConnected) {
                    WalletConnected(
                        state = state,
                        disconnectWallet = disconnectWallet
                    )
                } else {
                    WalletDisconnected()
                }
            }
        }
    )
}

@Composable
fun WalletConnected(state: WalletContract.UiState, disconnectWallet: () -> Unit) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .border(
                width = 7.dp,
                color = AppTheme.extraColorScheme.successBright,
                shape = CircleShape
            )
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bitcoin_wallet),
            contentDescription = stringResource(id = R.string.settings_wallet_not_connected_title),
            alignment = Alignment.Center,
            colorFilter = ColorFilter.tint(
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4
            )
        )

        Image(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.BottomEnd)
                .border(
                    width = 6.dp,
                    color = AppTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ),
            colorFilter = ColorFilter.tint(
                color = AppTheme.extraColorScheme.successBright
            )
        )
    }
    Spacer(modifier = Modifier.height(50.dp))
    Text(
        text = stringResource(id = R.string.settings_wallet_connected_subtitle),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .height(88.dp)
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt,
                shape = RoundedCornerShape(size = 12.dp)
            ),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = state.nwc?.relayUrl ?: "",
            textAlign = TextAlign.Center
        )
        Divider()
        Text(
            text = state.nwc?.lud16 ?: "",
            textAlign = TextAlign.Center
        )
    }
    Spacer(modifier = Modifier.height(20.dp))
    PrimalLoadingButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 20.dp),
        text = stringResource(id = R.string.settings_wallet_disconnect_action),
        onClick = disconnectWallet
    )
}

@Composable
fun WalletDisconnected() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bitcoin_wallet),
            contentDescription = stringResource(id = R.string.settings_wallet_not_connected_title),
            alignment = Alignment.Center,
            colorFilter = ColorFilter.tint(
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4
            )
        )
    }
    Spacer(modifier = Modifier.height(50.dp))
    Text(
        text = stringResource(id = R.string.settings_wallet_not_connected_subtitle),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))
    val uriHandler = LocalUriHandler.current
    ConnectAlbyWalletButton(
        text = "Connect Alby Wallet",
        onClick = {
            uriHandler.openUri("https://nwc.getalby.com/apps/new?c=Primal-Android")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )
    Spacer(modifier = Modifier.height(20.dp))
    ConnectThirdPartyWalletButton(
        text = "Connect Other Wallet",
        onClick = {
            // open modal to input url or navigate to other page
            // do nothing for now
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )
}

@Preview()
@Composable
fun PreviewSettingsWalletScreen() {
    PrimalTheme {
        WalletScreen(
            state = WalletContract.UiState(
                nwcUrl = "nostr+walletconnect://69effe7b49a6dd5cf525bd0905917a5005ffe480b58eeb8e861418cf3ae760d9?relay=wss://relay.getalby.com/v1&secret=7c0dabd065b2de3299a0d0e1c26b8ac7047dae6b20aba3a62b23650eb601bbfd&lud16=nikola@getalby.com",
                isWalletConnected = true,
                nwc = NostrWalletConnect(
                    relayUrl = "wss://relay.getalby.com/v1",
                    lud16 = "miljan@getalby.com",
                    pubkey = "69effe7b49a6dd5cf525bd0905917a5005ffe480b58eeb8e861418cf3ae760d9",
                    keypair = HexVariant.nostrKeypair(
                        hexPubkey = "69effe7b49a6dd5cf525bd0905917a5005ffe480b58eeb8e861418cf3ae760d9",
                        hexPrivkey = "7c0dabd065b2de3299a0d0e1c26b8ac7047dae6b20aba3a62b23650eb601bbfd"
                    )
                )
            ),
            onClose = { },
            disconnectWallet = { }
        )
    }
}
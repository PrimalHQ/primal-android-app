package net.primal.android.settings.wallet.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.settings.wallet.WalletContract
import net.primal.android.settings.wallet.WalletViewModel
import net.primal.android.theme.PrimalTheme

@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
    onClose: () -> Unit
) {
    val uiState = viewModel.state.collectAsState()

    WalletScreen(
        state = uiState.value,
        onClose = onClose
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    state: WalletContract.UiState,
    onClose: () -> Unit
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
                Image(
                    painter = painterResource(id = R.drawable.nwc_disconnected_light),
                    contentDescription = stringResource(id = R.string.settings_wallet_not_connected_title),
                    alignment = Alignment.Center
                )
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
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                ConnectThirdPartyWalletButton(
                    text = "Connect Other Wallet",
                    onClick = {
                        // open modal to input url or navigate to other page
                        // do nothing for now
                    },
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }
        }
    )
}

@Preview(
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
fun PreviewSettingsWalletScreen() {
    PrimalTheme {
        WalletScreen(
            state = WalletContract.UiState(
                nwcUrl = "nostr+walletconnect://69effe7b49a6dd5cf525bd0905917a5005ffe480b58eeb8e861418cf3ae760d9?relay=wss://relay.getalby.com/v1&secret=7c0dabd065b2de3299a0d0e1c26b8ac7047dae6b20aba3a62b23650eb601bbfd&lud16=nikola@getalby.com"
            ),
            onClose = { },
        )
    }
}
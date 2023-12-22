package net.primal.android.settings.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.settings.wallet.WalletSettingsContract.UiEvent
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.user.domain.NostrWalletKeypair
import net.primal.android.user.domain.WalletPreference

@Composable
fun WalletSettingsScreen(viewModel: WalletSettingsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    WalletSettingsScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSettingsScreen(
    state: WalletSettingsContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = if (state.wallet != null) {
                    stringResource(id = R.string.settings_wallet_connected_title)
                } else {
                    stringResource(id = R.string.settings_wallet_not_connected_title)
                },
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                NwcActivationSwitch(
                    preferredNwc = state.walletPreference == WalletPreference.NostrWalletConnect,
                    onNwcChecked = { nwcActivated ->
                        eventPublisher(
                            UiEvent.UpdateWalletPreference(
                                walletPreference = if (nwcActivated) {
                                    WalletPreference.NostrWalletConnect
                                } else {
                                    WalletPreference.PrimalWallet
                                },
                            ),
                        )
                    },
                )

                PrimalDivider(modifier = Modifier.padding(horizontal = 8.dp))

                val nwcModifier = if (state.walletPreference != WalletPreference.NostrWalletConnect) {
                    Modifier.graphicsLayer { alpha = 0.42f }
                } else {
                    Modifier
                }.then(
                    Modifier.fillMaxSize().padding(top = 32.dp),
                )

                if (state.wallet != null) {
                    WalletConnected(
                        modifier = nwcModifier,
                        state = state,
                        enabled = state.walletPreference == WalletPreference.NostrWalletConnect,
                        disconnectWallet = {
                            eventPublisher(UiEvent.DisconnectWallet)
                        },
                    )
                } else {
                    val uriHandler = LocalUriHandler.current
                    WalletDisconnected(
                        modifier = nwcModifier,
                        enabled = state.walletPreference == WalletPreference.NostrWalletConnect,
                        onAlbyConnectClick = {
                            uriHandler.openUri("https://nwc.getalby.com/apps/new?c=Primal-Android")
                        },
                        onMutinyConnectClick = {
                            uriHandler.openUri(
                                "https://app.mutinywallet.com/settings/connections" +
                                    "?callbackUri=primal&name=Primal-Android",
                            )
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun NwcActivationSwitch(preferredNwc: Boolean, onNwcChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .padding(bottom = 5.dp),
            text = stringResource(id = R.string.settings_wallet_nwc_activation),
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        PrimalSwitch(
            checked = preferredNwc,
            onCheckedChange = onNwcChecked,
        )
    }
}

@Composable
fun WalletConnected(
    state: WalletSettingsContract.UiState,
    disconnectWallet: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WalletImage(
            modifier = Modifier.size(200.dp),
            connected = true,
        )

        Text(
            modifier = Modifier.padding(32.dp),
            text = stringResource(id = R.string.settings_wallet_connected_subtitle),
            textAlign = TextAlign.Center,
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(88.dp)
                .background(
                    color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    shape = RoundedCornerShape(size = 12.dp),
                ),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = state.wallet?.relays?.first() ?: "",
                textAlign = TextAlign.Center,
            )
            PrimalDivider()
            Text(
                text = state.wallet?.lightningAddress ?: state.userLightningAddress ?: "",
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        PrimalLoadingButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            text = stringResource(id = R.string.settings_wallet_disconnect_action),
            onClick = if (enabled) disconnectWallet else null,
        )
    }
}

@Composable
fun WalletDisconnected(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onAlbyConnectClick: () -> Unit,
    onMutinyConnectClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WalletImage(
            modifier = Modifier.size(200.dp),
        )

        Text(
            modifier = Modifier.padding(32.dp),
            text = stringResource(id = R.string.settings_wallet_not_connected_subtitle),
            textAlign = TextAlign.Center,
        )

        ConnectAlbyWalletButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            onClick = if (enabled) onAlbyConnectClick else null,
        )
        Spacer(modifier = Modifier.height(20.dp))
        ConnectMutinyWalletButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            onClick = if (enabled) onMutinyConnectClick else null,
        )
    }
}

@Composable
fun WalletImage(modifier: Modifier = Modifier, connected: Boolean = false) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    shape = CircleShape,
                )
                .border(
                    width = if (connected) 8.dp else 0.dp,
                    color = if (connected) {
                        AppTheme.extraColorScheme.successBright
                    } else {
                        Color.Unspecified
                    },
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.bitcoin_wallet),
                contentDescription = null,
                alignment = Alignment.Center,
                colorFilter = ColorFilter.tint(
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                ),
            )
        }

        if (connected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                modifier = Modifier
                    .background(
                        color = AppTheme.extraColorScheme.successBright,
                        shape = CircleShape,
                    )
                    .border(
                        width = 6.dp,
                        color = AppTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    )
                    .size(56.dp)
                    .padding(all = 8.dp)
                    .align(Alignment.BottomEnd),
                tint = Color.White,
            )
        }
    }
}

private val albyColor = Color(0xFFFFDF6F)

@Composable
fun ConnectAlbyWalletButton(modifier: Modifier = Modifier, onClick: (() -> Unit)?) {
    PrimalFilledButton(
        modifier = modifier,
        containerColor = albyColor,
        onClick = onClick,
    ) {
        IconText(
            text = stringResource(id = R.string.settings_wallet_connect_alby_wallet),
            leadingIcon = ImageVector.vectorResource(id = R.drawable.alby_logo),
            iconSize = 42.sp,
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            leadingIconTintColor = null,
        )
    }
}

private val mutinyColor = Color(0xFF4F1425)

@Composable
fun ConnectMutinyWalletButton(modifier: Modifier = Modifier, onClick: (() -> Unit)?) {
    PrimalFilledButton(
        modifier = modifier,
        containerColor = mutinyColor,
        onClick = onClick,
    ) {
        IconText(
            text = stringResource(id = R.string.settings_wallet_connect_mutiny_wallet),
            leadingIcon = ImageVector.vectorResource(id = R.drawable.mutiny_logo),
            iconSize = 42.sp,
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            leadingIconTintColor = null,
        )
    }
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
            ),
            WalletSettingsContract.UiState(
                wallet = null,
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
            eventPublisher = {},
        )
    }
}

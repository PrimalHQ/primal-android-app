package net.primal.android.settings.wallet.settings.ui

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.ext.openUriSafely
import net.primal.android.settings.wallet.settings.WalletSettingsContract
import net.primal.android.settings.wallet.settings.WalletUiStateProvider
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.domain.NostrWalletConnect

@Composable
fun ExternalWalletSettings(
    nwcWallet: NostrWalletConnect?,
    onExternalWalletDisconnect: () -> Unit,
    onOtherConnectClick: () -> Unit,
) {
    Column(
        modifier = Modifier.animateContentSize(),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        ExternalWalletSection(
            nwcWallet = nwcWallet,
            onExternalWalletDisconnect = onExternalWalletDisconnect,
            onOtherConnectClick = onOtherConnectClick,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ExternalWalletSection(
    nwcWallet: NostrWalletConnect?,
    onExternalWalletDisconnect: () -> Unit,
    onOtherConnectClick: () -> Unit,
) {
    SectionTitle(
        title = if (nwcWallet != null) {
            stringResource(id = R.string.settings_wallet_nwc_header_connected)
        } else {
            stringResource(id = R.string.settings_wallet_nwc_header_not_connected)
        },
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        text = stringResource(id = R.string.settings_wallet_nwc_header_not_connected_hint),
        style = AppTheme.typography.bodySmall,
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (nwcWallet != null) {
        ExternalWalletConnected(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            relay = nwcWallet.relays.firstOrNull() ?: "",
            lightningAddress = nwcWallet.lightningAddress ?: "",
            disconnectWallet = onExternalWalletDisconnect,
        )
    } else {
        val uriHandler = LocalUriHandler.current
        ExternalWalletDisconnected(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            onAlbyConnectClick = {
                uriHandler.openUriSafely(
                    "https://nwc.getalby.com/apps/new?c=Primal-Android",
                )
            },
            onOtherConnectClick = onOtherConnectClick,
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        text = title.uppercase(),
        style = AppTheme.typography.bodySmall,
    )
}

@Composable
private fun ExternalWalletConnected(
    relay: String,
    lightningAddress: String,
    disconnectWallet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .height(100.dp)
                .background(
                    color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    shape = RoundedCornerShape(size = 12.dp),
                ),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = relay,
                textAlign = TextAlign.Center,
            )
            PrimalDivider()
            IconText(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        clipboardManager.setText(AnnotatedString(text = lightningAddress))
                    },
                ),
                text = lightningAddress,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        PrimalLoadingButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            text = stringResource(id = R.string.settings_wallet_nwc_disconnect_wallet_button),
            onClick = disconnectWallet,
        )
    }
}

@Composable
private fun ExternalWalletDisconnected(
    modifier: Modifier = Modifier,
    onAlbyConnectClick: () -> Unit,
    onOtherConnectClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ConnectAlbyWalletButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = onAlbyConnectClick,
        )

        Spacer(modifier = Modifier.height(12.dp))

        ConnectOtherWalletButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = onOtherConnectClick,
        )
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
            text = stringResource(id = R.string.settings_wallet_nwc_connect_alby_wallet),
            leadingIcon = ImageVector.vectorResource(id = R.drawable.alby_logo),
            iconSize = 42.sp,
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            leadingIconTintColor = null,
        )
    }
}

@Composable
fun ConnectOtherWalletButton(modifier: Modifier = Modifier, onClick: (() -> Unit)?) {
    PrimalFilledButton(
        modifier = modifier,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        onClick = onClick,
    ) {
        IconText(
            text = stringResource(id = R.string.settings_wallet_nwc_scan_qr_code),
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colorScheme.onSurface,
        )
    }
}

@Preview
@Composable
private fun PreviewExternalWalletSettings(
    @PreviewParameter(WalletUiStateProvider::class)
    state: WalletSettingsContract.UiState,
) {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        Surface {
            Column {
                ExternalWalletSettings(
                    nwcWallet = state.wallet,
                    onExternalWalletDisconnect = {},
                    onOtherConnectClick = {},
                )
            }
        }
    }
}

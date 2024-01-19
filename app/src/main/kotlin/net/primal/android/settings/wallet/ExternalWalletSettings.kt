package net.primal.android.settings.wallet

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.user.domain.WalletPreference

@Composable
fun ExternalWalletSettings(
    nwcWallet: NostrWalletConnect?,
    walletPreference: WalletPreference,
    profileLightningAddress: String?,
    onUsePrimalWalletSwitchChanged: (Boolean) -> Unit,
    onExternalWalletDisconnect: () -> Unit,
    onEditProfileClick: () -> Unit,
) {
    Column(
        modifier = Modifier.animateContentSize(),
    ) {
        val primalWalletPreferred = walletPreference != WalletPreference.NostrWalletConnect
        ExternalWalletListItem(
            preferPrimalWallet = primalWalletPreferred,
            onExternalWalletSwitchChanged = onUsePrimalWalletSwitchChanged,
        )

        if (!primalWalletPreferred) {
            Spacer(modifier = Modifier.height(24.dp))

            ExternalWalletSection(
                nwcWallet = nwcWallet,
                onExternalWalletDisconnect = onExternalWalletDisconnect,
            )

            Spacer(modifier = Modifier.height(24.dp))

            NostrProfileLightingAddressSection(
                lightningAddress = profileLightningAddress,
                onEditProfileClick = onEditProfileClick,
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
    )
}

@Composable
private fun ExternalWalletSection(nwcWallet: NostrWalletConnect?, onExternalWalletDisconnect: () -> Unit) {
    SectionTitle(
        title = if (nwcWallet != null) {
            stringResource(id = R.string.settings_wallet_nwc_header_connected)
        } else {
            stringResource(id = R.string.settings_wallet_nwc_header_not_connected)
        },
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
                uriHandler.openUri(
                    "https://nwc.getalby.com/apps/new?c=Primal-Android",
                )
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
    onMutinyConnectClick: () -> Unit,
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

        ConnectMutinyWalletButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = onMutinyConnectClick,
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

private val mutinyColor = Color(0xFF4F1425)

@Composable
fun ConnectMutinyWalletButton(modifier: Modifier = Modifier, onClick: (() -> Unit)?) {
    PrimalFilledButton(
        modifier = modifier,
        containerColor = mutinyColor,
        onClick = onClick,
    ) {
        IconText(
            text = stringResource(id = R.string.settings_wallet_nwc_connect_mutiny_wallet),
            leadingIcon = ImageVector.vectorResource(id = R.drawable.mutiny_logo),
            iconSize = 42.sp,
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            leadingIconTintColor = null,
        )
    }
}

@Composable
private fun NostrProfileLightingAddressSection(lightningAddress: String?, onEditProfileClick: () -> Unit) {
    Column {
        SectionTitle(
            title = stringResource(id = R.string.settings_wallet_nwc_profile_lightning_address),
        )

        Card(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            ),
        ) {
            ListItem(
                modifier = Modifier.clickable { onEditProfileClick() },
                colors = ListItemDefaults.colors(
                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                ),
                headlineContent = {
                    Text(
                        text = "Address",
                        style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                    )
                },
                trailingContent = {
                    Text(
                        text = lightningAddress ?: "",
                        style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                    )
                },
            )
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(horizontal = 4.dp)
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

@Preview
@Composable
private fun PreviewExternalWalletSettings(
    @PreviewParameter(WalletUiStateProvider::class)
    state: WalletSettingsContract.UiState,
) {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        Surface {
            Column {
                ExternalWalletSettings(
                    nwcWallet = state.wallet,
                    walletPreference = state.walletPreference,
                    profileLightningAddress = state.userLightningAddress,
                    onUsePrimalWalletSwitchChanged = {},
                    onExternalWalletDisconnect = {},
                    onEditProfileClick = {},
                )
            }
        }
    }
}

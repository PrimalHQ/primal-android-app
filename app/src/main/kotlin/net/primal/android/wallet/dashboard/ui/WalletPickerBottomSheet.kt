package net.primal.android.wallet.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.core.utils.hideAndRun
import net.primal.android.theme.AppTheme
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.domain.wallet.Wallet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletPickerBottomSheet(
    modifier: Modifier = Modifier,
    wallets: List<Wallet>,
    activeWallet: Wallet,
    onDismissRequest: () -> Unit,
    onConfigureWalletsClick: () -> Unit,
    onActiveWalletChanged: (Wallet) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        contentColor = AppTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = modifier.background(AppTheme.extraColorScheme.surfaceVariantAlt2),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WalletPickerTopBar()
            WalletPickerContent(
                wallets = wallets,
                onActiveWalletChanged = {
                    sheetState.hideAndRun(coroutineScope = scope, onDismissRequest = onDismissRequest) {
                        onActiveWalletChanged(it)
                    }
                },
                activeWallet = activeWallet,
            )
            Spacer(modifier = Modifier.height(150.dp))
            WalletPickerBottomBar(
                onConfigureWalletsClick = {
                    sheetState.hideAndRun(coroutineScope = scope, onDismissRequest = onDismissRequest) {
                        onConfigureWalletsClick()
                    }
                },
            )
        }
    }
}

@Composable
private fun WalletPickerContent(
    wallets: List<Wallet>,
    onActiveWalletChanged: (Wallet) -> Unit,
    activeWallet: Wallet,
) {
    Column(
        modifier = Modifier
            .background(color = AppTheme.extraColorScheme.surfaceVariantAlt2)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        wallets.forEach { wallet ->
            WalletListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(AppTheme.shapes.large)
                    .clickable(onClick = { onActiveWalletChanged(wallet) }),
                wallet = wallet,
                selected = wallet.walletId == activeWallet.walletId,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun WalletPickerTopBar() {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        ),
        title = { Text(text = stringResource(id = R.string.wallet_picker_title)) },
    )
}

@Composable
private fun WalletPickerBottomBar(onConfigureWalletsClick: () -> Unit) {
    PrimalDivider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(onClick = onConfigureWalletsClick) {
            Text(text = stringResource(id = R.string.wallet_picker_configure_wallets))
        }
    }
}

private const val LightningAddressEllipsizeThreshold = 55

@Suppress("CyclomaticComplexMethod")
@Composable
private fun WalletListItem(
    modifier: Modifier = Modifier,
    wallet: Wallet,
    selected: Boolean,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    ListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(
            containerColor = if (selected) {
                AppTheme.extraColorScheme.surfaceVariantAlt1
            } else {
                AppTheme.extraColorScheme.surfaceVariantAlt2
            },
        ),
        headlineContent = {
            Text(
                text = when (wallet) {
                    is Wallet.NWC -> stringResource(id = R.string.wallet_nwc_title)
                    is Wallet.Primal -> stringResource(id = R.string.wallet_primal_title)
                    is Wallet.Spark -> stringResource(id = R.string.wallet_spark_title)
                },
                style = AppTheme.typography.bodyLarge,
                maxLines = 1,
                color = AppTheme.colorScheme.onSurface,
            )
        },
        supportingContent = {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = wallet.lightningAddress?.let {
                    if (it.length >= LightningAddressEllipsizeThreshold) {
                        it.ellipsizeMiddle(size = 20)
                    } else {
                        it
                    }
                } ?: when (wallet) {
                    is Wallet.NWC -> stringResource(id = R.string.wallet_nwc_description)
                    is Wallet.Primal -> stringResource(id = R.string.wallet_primal_description)
                    is Wallet.Spark -> stringResource(id = R.string.wallet_primal_description)
                },
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = 15.sp),
            )
        },
        trailingContent = {
            val walletBalance = wallet.balanceInBtc?.let { numberFormat.format(it.toSats().toLong()) }
            if (walletBalance != null) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                ) {
                    Text(
                        text = walletBalance,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = stringResource(id = R.string.wallet_sats_suffix),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    )
                }
            }
        },
    )
}

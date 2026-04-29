package net.primal.android.wallet.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.PrimalOverlayBottomBar
import net.primal.android.core.compose.PrimalOverlayCloseButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.LightningBolt
import net.primal.android.core.compose.icons.primaliconpack.LightningBoltFilled
import net.primal.android.core.compose.picker.BasePickerListItem
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.picker.WalletPickerContract.UiEvent
import net.primal.android.wallet.picker.WalletPickerContract.UiState
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.domain.wallet.UserWallet
import net.primal.domain.wallet.Wallet

@Composable
fun WalletPickerOverlayContent(onDismiss: () -> Unit) {
    val viewModel = hiltViewModel<WalletPickerViewModel>()
    val state = viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val errorMessage = stringResource(id = R.string.wallet_picker_reassign_error)
    LaunchedEffect(state.value.error) {
        if (state.value.error != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.setEvent(UiEvent.DismissError)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt2)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WalletPickerContent(
            state = state.value,
            onWalletClick = { userWallet ->
                if (state.value.isEditMode) {
                    viewModel.setEvent(UiEvent.SelectWalletForReassignment(userWallet))
                } else {
                    viewModel.setEvent(UiEvent.ChangeActiveWallet(userWallet))
                    onDismiss()
                }
            },
        )

        Spacer(modifier = Modifier.weight(1f))

        SnackbarHost(hostState = snackbarHostState)

        WalletPickerBottomBar(
            isEditMode = state.value.isEditMode,
            onConfigureClick = { viewModel.setEvent(UiEvent.EnterEditMode) },
            onCancelClick = { viewModel.setEvent(UiEvent.CancelEditMode) },
            onDoneClick = { viewModel.setEvent(UiEvent.ConfirmReassignment) },
            onCloseClick = onDismiss,
        )
    }
}

@Composable
private fun WalletPickerContent(state: UiState, onWalletClick: (UserWallet) -> Unit) {
    val effectiveRegisteredId = if (state.isEditMode) {
        state.previewRegisteredWalletId
    } else {
        state.registeredWalletId
    }

    Column(
        modifier = Modifier
            .background(color = AppTheme.extraColorScheme.surfaceVariantAlt2)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        state.wallets.forEach { userWallet ->
            val wallet = userWallet.wallet
            val isNwc = wallet is Wallet.NWC
            val isNotTappableInEditMode = state.isEditMode && isNwc

            val lightningAddress = if (state.isEditMode && !isNwc) {
                if (wallet.walletId == effectiveRegisteredId) {
                    state.registeredLightningAddress
                } else {
                    null
                }
            } else {
                userWallet.lightningAddress
            }

            WalletListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(AppTheme.shapes.large)
                    .then(
                        if (!isNotTappableInEditMode) {
                            Modifier.clickable(onClick = { onWalletClick(userWallet) })
                        } else {
                            Modifier
                        },
                    ),
                wallet = wallet,
                lightningAddressOverride = lightningAddress,
                selected = !state.isEditMode && wallet.walletId == state.activeWalletId,
                showBoltIcon = state.isEditMode,
                boltIconAlpha = if (isNwc) 0f else 1f,
                isLnRegistered = wallet.walletId == effectiveRegisteredId,
            )
        }
    }
}

@Composable
private fun WalletPickerBottomBar(
    isEditMode: Boolean,
    onConfigureClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDoneClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    if (isEditMode) {
        PrimalOverlayBottomBar(
            leading = {
                TextButton(onClick = onCancelClick) {
                    Text(text = stringResource(id = R.string.wallet_picker_cancel))
                }
            },
            trailing = {
                TextButton(onClick = onDoneClick) {
                    Text(text = stringResource(id = R.string.wallet_picker_done))
                }
            },
        )
    } else {
        PrimalOverlayBottomBar(
            leading = {
                TextButton(onClick = onConfigureClick) {
                    Text(text = stringResource(id = R.string.wallet_picker_configure_wallets))
                }
            },
            trailing = { PrimalOverlayCloseButton(onClick = onCloseClick) },
        )
    }
}

private const val LightningAddressEllipsizeThreshold = 55

@Suppress("CyclomaticComplexMethod", "LongMethod")
@Composable
private fun WalletListItem(
    modifier: Modifier = Modifier,
    wallet: Wallet,
    lightningAddressOverride: String? = null,
    selected: Boolean,
    showBoltIcon: Boolean = false,
    boltIconAlpha: Float = 1f,
    isLnRegistered: Boolean = false,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    BasePickerListItem(
        modifier = modifier,
        title = wallet.displayName(),
        selected = selected,
        supportingContent = {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = lightningAddressOverride?.let {
                    if (it.length >= LightningAddressEllipsizeThreshold) {
                        it.ellipsizeMiddle(size = 20)
                    } else {
                        it
                    }
                } ?: when (wallet) {
                    is Wallet.NWC -> stringResource(id = R.string.wallet_picker_nwc_description)
                    is Wallet.Primal -> stringResource(id = R.string.wallet_picker_no_lightning_address)
                    is Wallet.Spark -> stringResource(id = R.string.wallet_picker_no_lightning_address)
                },
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = 15.sp),
            )
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
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
                if (showBoltIcon) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .size(24.dp)
                            .alpha(boltIconAlpha),
                        imageVector = if (isLnRegistered) {
                            PrimalIcons.LightningBoltFilled
                        } else {
                            PrimalIcons.LightningBolt
                        },
                        contentDescription = null,
                        tint = if (isLnRegistered) {
                            AppTheme.colorScheme.primary
                        } else {
                            AppTheme.extraColorScheme.onSurfaceVariantAlt3
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun Wallet.displayName(): String =
    when (this) {
        is Wallet.NWC -> stringResource(id = R.string.wallet_picker_nwc_title)
        is Wallet.Primal -> stringResource(id = R.string.wallet_picker_legacy_title)
        is Wallet.Spark -> stringResource(id = R.string.wallet_picker_spark_title)
    }

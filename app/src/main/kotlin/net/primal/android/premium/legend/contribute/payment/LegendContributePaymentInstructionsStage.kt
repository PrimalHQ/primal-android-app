package net.primal.android.premium.legend.contribute.payment

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import net.primal.android.R
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.legend.become.PrimalLegendAmount
import net.primal.android.premium.legend.become.amount.NoPaymentInstructionsColumn
import net.primal.android.premium.legend.contribute.LegendContributeContract
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiState
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.domain.Network
import net.primal.android.wallet.transactions.receive.QrCodeBox
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegendContributePaymentInstructionsStage(
    modifier: Modifier,
    state: UiState,
    onBack: () -> Unit,
    onPaymentInstructionRetry: () -> Unit,
    onPrimalWalletPayment: () -> Unit,
    onErrorDismiss: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = {
            when (it) {
                is UiState.ContributionError.PaymentInstructionFetchFailed ->
                    it.cause.message.toString()
                is UiState.ContributionError.WithdrawViaPrimalWalletFailed ->
                    it.cause.message.toString()
            }
        },
        onErrorDismiss = onErrorDismiss,
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            PrimalTopAppBar(
                title = state.paymentMethod.resolveTitle(),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onBack,
                showDivider = true,
            )
        },
        bottomBar = {
            LegendContributePaymentStageBottomBar(
                state = state,
                enabled = state.arePaymentInstructionsAvailable(),
                onPrimalWalletPayment = onPrimalWalletPayment,
                primalWalletPaymentInProgress = state.primalWalletPaymentInProgress,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        ) {
            Box(modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center) {
                when {
                    state.isFetchingPaymentInstructions -> PrimalLoadingSpinner()
                    state.arePaymentInstructionsAvailable() -> QrCodeBox(
                        qrCodeValue = state.qrCodeValue,
                        network = when (state.paymentMethod) {
                            LegendContributeContract.PaymentMethod.OnChainBitcoin -> Network.Bitcoin
                            else -> Network.Lightning
                        },
                    )

                    else -> NoPaymentInstructionsColumn(onRetryClick = onPaymentInstructionRetry)
                }
            }

            PrimalLegendAmount(
                btcValue = BigDecimal(state.amountInSats.toULong().toBtc()),
                exchangeBtcUsdRate = state.currentExchangeRate,
                coerceMinAmount = true,
            )

            Text(
                modifier = Modifier.padding(horizontal = 48.dp),
                text = state.paymentMethod.resolveDescriptionText(),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyMedium,
                fontSize = 17.sp,
                lineHeight = 23.sp,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }
    }
}

@Composable
private fun LegendContributeContract.PaymentMethod?.resolveTitle(): String {
    return when (this) {
        LegendContributeContract.PaymentMethod.OnChainBitcoin ->
            stringResource(id = R.string.legend_contribution_payment_instructions_on_chain_stage_title)

        LegendContributeContract.PaymentMethod.BitcoinLightning ->
            stringResource(id = R.string.legend_contribution_payment_instructions_lightning_stage_title)

        null -> ""
    }
}

@Composable
private fun LegendContributeContract.PaymentMethod?.resolveDescriptionText(): String {
    return when (this) {
        LegendContributeContract.PaymentMethod.OnChainBitcoin ->
            stringResource(R.string.premium_become_legend_payment_instruction_on_chain)

        LegendContributeContract.PaymentMethod.BitcoinLightning ->
            stringResource(R.string.premium_become_legend_payment_instruction_lightning)

        null -> ""
    }
}

@Composable
private fun LegendContributePaymentStageBottomBar(
    state: UiState,
    enabled: Boolean,
    onPrimalWalletPayment: () -> Unit,
    primalWalletPaymentInProgress: Boolean,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PrimalLoadingButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.legend_contribution_payment_instructions_copy_invoice),
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            contentColor = AppTheme.colorScheme.onSurface,
            enabled = enabled,
            onClick = {
                val clipboard = context.getSystemService(ClipboardManager::class.java)
                val clip = ClipData.newPlainText("", state.qrCodeValue)
                clipboard.setPrimaryClip(clip)
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimalLoadingButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            loading = primalWalletPaymentInProgress,
            text = stringResource(id = R.string.legend_contribution_payment_instructions_pay_with_primal_wallet),
            onClick = onPrimalWalletPayment,
        )
    }
}

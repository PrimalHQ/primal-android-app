package net.primal.android.premium.legend.contribute.amount

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.numericpad.PrimalNumericPad
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiState
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.domain.CurrencyMode
import net.primal.android.wallet.ui.TransactionAmountText
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegendContributeAmountStage(
    modifier: Modifier,
    state: UiState,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onAmountClick: () -> Unit,
    onAmountChanged: (String) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.legend_contribution_amount_stage_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onBack,
                showDivider = true,
            )
        },
        bottomBar = {
            LegendContributeAmountStageBottomBar(
                state = state,
                onBack = onBack,
                onNext = onNext,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TransactionHeader(
                state = state,
                onAmountClick = onAmountClick,
            )

            PrimalNumericPad(
                modifier = Modifier.fillMaxWidth(),
                amountInSats = if (state.currencyMode == CurrencyMode.SATS) {
                    state.amountInSats
                } else {
                    state.amountInUsd
                },
                currencyMode = state.currencyMode,
                maximumUsdAmount = state.maximumUsdAmount,
                onAmountInSatsChanged = { newAmount ->
                    onAmountChanged(newAmount)
                },
            )
        }
    }
}

@Composable
private fun LegendContributeAmountStageBottomBar(
    state: UiState,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TransactionActionsRow(
            onCancelClick = onBack,
            onActionClick = onNext,
            nextEnabled = state.amountInSats != "0",
        )
    }
}

@Composable
private fun TransactionHeader(state: UiState, onAmountClick: () -> Unit) {
    Column {
        Spacer(modifier = Modifier.height(100.dp))

        TransactionAmountText(
            amountInBtc = state.amountInSats.toULong().toBtc().toString(),
            amountInUsd = state.amountInUsd,
            currentExchangeRate = state.currentExchangeRate,
            currentCurrencyMode = state.currencyMode,
            onAmountClick = onAmountClick,
        )
    }
}

@Composable
private fun TransactionActionsRow(
    nextEnabled: Boolean,
    onCancelClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        PrimalLoadingButton(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.wallet_create_transaction_cancel_numeric_pad_button),
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            contentColor = AppTheme.colorScheme.onSurface,
            onClick = onCancelClick,
        )

        Spacer(
            modifier = Modifier
                .animateContentSize()
                .width(16.dp),
        )

        PrimalLoadingButton(
            modifier = Modifier.weight(1f),
            enabled = nextEnabled,
            text = stringResource(id = R.string.wallet_create_transaction_next_numeric_pad_button),
            onClick = onActionClick,
        )
    }
}

package net.primal.android.premium.legend.contribute.payment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.legend.contribute.LegendContributeContract
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegendContributePaymentInstructionsStage(
    modifier: Modifier,
    state: UiState,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PrimalTopAppBar(
                title = if (state.paymentMethod == LegendContributeContract.PaymentMethod.OnChainBitcoin) {
                    stringResource(id = R.string.legend_contribution_payment_instructions_on_chain_stage_title)
                } else {
                    stringResource(id = R.string.legend_contribution_payment_instructions_lightning_stage_title)
                },
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onBack,
                showDivider = true,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable { onNext() },
        ) {
        }
    }
}

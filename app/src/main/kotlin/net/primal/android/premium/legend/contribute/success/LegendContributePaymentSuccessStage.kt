package net.primal.android.premium.legend.contribute.success

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
import net.primal.android.premium.ui.PaymentSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegendContributePaymentSuccessStage(modifier: Modifier, onBack: () -> Unit) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.legend_contribution_success_stage_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onBack,
                showDivider = true,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            PaymentSuccess(
                modifier = modifier,
                title = stringResource(R.string.premium_become_legend_success_title),
                headlineText = stringResource(R.string.premium_become_legend_success_headline),
                supportText = stringResource(R.string.premium_become_legend_success_supporting_text),
                buttonText = stringResource(R.string.premium_become_legend_success_done_button),
                onDoneClick = onBack,
            )
        }
    }
}

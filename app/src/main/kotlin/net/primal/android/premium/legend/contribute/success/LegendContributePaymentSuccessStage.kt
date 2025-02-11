package net.primal.android.premium.legend.contribute.success

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.premium.ui.PaymentSuccess

@ExperimentalMaterial3Api
@Composable
fun LegendContributePaymentSuccessStage(modifier: Modifier, onBack: () -> Unit) {
    PaymentSuccess(
        modifier = modifier.fillMaxSize(),
        title = stringResource(R.string.legend_contribution_success_stage_title),
        headlineText = stringResource(R.string.legend_contribution_success_stage_appreciation_title),
        supportText = stringResource(R.string.legend_contribution_success_stage_appreciation_subtitle),
        buttonText = stringResource(R.string.legend_contribution_success_stage_appreciation_done_button),
        onDoneClick = onBack,
    )
}

package net.primal.android.premium.buying.success

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.premium.ui.PaymentSuccess

@ExperimentalMaterial3Api
@Composable
fun PremiumBuyingSuccessStage(modifier: Modifier, onDoneClick: () -> Unit) {
    PaymentSuccess(
        modifier = modifier,
        title = stringResource(R.string.premium_success_purchase_title),
        headlineText = stringResource(R.string.premium_success_purchase_headline),
        supportText = stringResource(R.string.premium_success_purchase_supporting_text),
        buttonText = stringResource(R.string.premium_success_purchase_done_button),
        onDoneClick = onDoneClick,
    )
}

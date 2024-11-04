package net.primal.android.premium.info.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.primal.android.R

@Composable
fun WhyPremiumTabContent(modifier: Modifier = Modifier) {
    QAColumn(
        modifier = modifier,
        question = stringResource(id = R.string.premium_more_info_why_premium_question),
        answer = stringResource(id = R.string.premium_more_info_why_premium_answer).trimIndent(),
    )
}

package net.primal.android.premium.info.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.core.compose.QAList

@Composable
fun FAQTabContent(modifier: Modifier = Modifier, contentPadding: PaddingValues) {
    val questionAndAnswerPairs = listOf(
        Pair(
            stringResource(id = R.string.premium_more_info_faq_first_question),
            stringResource(id = R.string.premium_more_info_faq_first_answer),
        ),
        Pair(
            stringResource(id = R.string.premium_more_info_faq_second_question),
            stringResource(id = R.string.premium_more_info_faq_second_answer),
        ),
        Pair(
            stringResource(id = R.string.premium_more_info_faq_third_question),
            stringResource(id = R.string.premium_more_info_faq_third_answer),
        ),
        Pair(
            stringResource(id = R.string.premium_more_info_faq_fourth_question),
            stringResource(id = R.string.premium_more_info_faq_fourth_answer),
        ),
        Pair(
            stringResource(id = R.string.premium_more_info_faq_fifth_question),
            stringResource(id = R.string.premium_more_info_faq_fifth_answer),
        ),
        Pair(
            stringResource(id = R.string.premium_more_info_faq_sixth_question),
            stringResource(id = R.string.premium_more_info_faq_sixth_answer),
        ),
        Pair(
            stringResource(id = R.string.premium_more_info_faq_seventh_question),
            stringResource(id = R.string.premium_more_info_faq_seventh_answer),
        ),
        Pair(
            stringResource(id = R.string.premium_more_info_faq_eighth_question),
            stringResource(id = R.string.premium_more_info_faq_eighth_answer),
        ),
        Pair(
            stringResource(id = R.string.premium_more_info_faq_ninth_question),
            stringResource(id = R.string.premium_more_info_faq_ninth_answer),
        ),
        Pair(
            stringResource(id = R.string.premium_more_info_faq_tenth_question),
            stringResource(id = R.string.premium_more_info_faq_tenth_answer),
        ),
        Pair(
            stringResource(id = R.string.premium_more_info_faq_eleventh_question),
            stringResource(id = R.string.premium_more_info_faq_eleventh_answer),
        ),
        Pair(
            stringResource(id = R.string.premium_more_info_faq_twelfth_question),
            stringResource(id = R.string.premium_more_info_faq_twelfth_answer),
        ),
        Pair(
            stringResource(id = R.string.premium_more_info_faq_thirteenth_question),
            stringResource(id = R.string.premium_more_info_faq_thirteenth_answer),
        ),
    )
    QAList(
        questionAndAnswerPairs = questionAndAnswerPairs,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

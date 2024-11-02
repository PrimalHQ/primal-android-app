package net.primal.android.premium.info.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R

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
    )
    LazyColumn(
        contentPadding = contentPadding,
        modifier = modifier,
    ) {
        item { Spacer(modifier = Modifier.height(20.dp)) }
        items(
            items = questionAndAnswerPairs,
            key = { it.first },
        ) { qaPair ->
            repeat(times = 2) {
                QAColumn(
                    question = qaPair.first,
                    answer = qaPair.second,
                )
                Spacer(modifier = Modifier.height(42.dp))
            }
        }
    }
}

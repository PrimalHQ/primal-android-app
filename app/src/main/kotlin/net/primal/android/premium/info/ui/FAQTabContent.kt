package net.primal.android.premium.info.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun FAQTabContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
) {
    val questionAndAnswerPairs = listOf(
        Pair(
            "Become a Nostr power user and help shape the future?",
            """
            At Primal, we don’t rely on advertising. We don’t monetize user data. Our users are our customers. Our sole focus is to make the best possible product for our users. We open source all our work to help the Nostr ecosystem flourish. By signing up for Primal Premium, you are enabling us to continue building for Nostr. 
        """.trimIndent(),
        ),
        Pair(
            "Open protocols like Nostr give us the opportunity to regain control over our online lives?",
            """
            At Primal, we don’t rely on advertising. We don’t monetize user data. Our users are our customers. Our sole focus is to make the best possible product for our users. We open source all our work to help the Nostr ecosystem flourish. By signing up for Primal Premium, you are enabling us to continue building for Nostr. 
        """.trimIndent(),
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


package net.primal.android.core.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.premium.info.ui.QAColumn

@Composable
fun QAList(
    questionAndAnswerPairs: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        contentPadding = contentPadding,
        modifier = modifier,
    ) {
        item { Spacer(modifier = Modifier.height(20.dp)) }
        items(
            items = questionAndAnswerPairs,
            key = { it.first },
        ) { qaPair ->
            QAColumn(
                question = qaPair.first,
                answer = qaPair.second,
            )
            Spacer(modifier = Modifier.height(42.dp))
        }
    }
}

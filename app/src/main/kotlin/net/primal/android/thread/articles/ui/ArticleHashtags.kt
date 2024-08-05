package net.primal.android.thread.articles.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@ExperimentalLayoutApi
@Composable
fun ArticleHashtags(hashtags: List<String>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        hashtags.forEach { hashtag ->
            SuggestionChip(
                modifier = Modifier.padding(horizontal = 3.dp),
                onClick = {},
                shape = AppTheme.shapes.extraLarge,
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = true,
                    borderColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                ),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    labelColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                ),
                label = {
                    Text(
                        modifier = Modifier.padding(vertical = 0.dp),
                        text = hashtag.substring(1),
                        style = AppTheme.typography.bodyMedium,
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview
private fun PreviewArticleHashtags() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        Surface {
            ArticleHashtags(
                hashtags = listOf("#primal", "#nostr", "#wallet", "#bitcoin", "#mobile", "#android", "#freedom"),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

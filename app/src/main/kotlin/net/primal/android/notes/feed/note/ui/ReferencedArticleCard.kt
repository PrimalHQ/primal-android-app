package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.articles.feed.ui.FeedArticleListItem
import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun ReferencedArticleCard(
    modifier: Modifier = Modifier,
    data: FeedArticleUi,
    colors: CardColors = referencedArticleCardColors(),
    onClick: (() -> Unit)? = null,
) {
    NoteSurfaceCard(
        modifier = modifier.wrapContentHeight(),
        colors = colors,
        border = BorderStroke(width = 0.5.dp, color = AppTheme.colorScheme.outline),
    ) {
        FeedArticleListItem(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            data = data,
            enabledDropdownMenu = false,
            showCommentsCount = false,
            color = colors.containerColor,
            onClick = if (onClick != null) {
                { onClick() }
            } else {
                null
            },
        )
    }
}

@Composable
private fun referencedArticleCardColors() =
    CardDefaults.cardColors(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
    )

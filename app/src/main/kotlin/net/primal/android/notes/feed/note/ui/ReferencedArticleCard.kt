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
    data: FeedArticleUi,
    modifier: Modifier = Modifier,
    hasBorder: Boolean = false,
    colors: CardColors = referencedArticleCardColors(),
    onClick: (() -> Unit)? = null,
) {
    NoteSurfaceCard(
        modifier = modifier.wrapContentHeight(),
        colors = colors,
        border = if (hasBorder) {
            BorderStroke(width = 0.5.dp, color = AppTheme.colorScheme.outline)
        } else {
            null
        },
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
fun referencedArticleCardColors() =
    CardDefaults.cardColors(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
    )

package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.Instant
import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.NoteNostrUriUi
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferencedArticlesColumn(
    articleResources: List<NoteNostrUriUi>,
    expanded: Boolean,
    containerColor: Color,
    noteCallbacks: NoteCallbacks,
    modifier: Modifier = Modifier,
    hasBorder: Boolean = false,
) {
    val displayableNotes = if (articleResources.isNotEmpty()) {
        if (expanded) articleResources else articleResources.subList(0, 1)
    } else {
        emptyList()
    }

    Column(modifier = modifier) {
        displayableNotes.forEach { nostrResourceUi ->
            val data = nostrResourceUi.referencedArticle
            checkNotNull(data)
            ReferencedArticleCard(
                modifier = Modifier.padding(vertical = 4.dp),
                data = FeedArticleUi(
                    aTag = data.aTag,
                    eventId = data.eventId,
                    articleId = data.articleId,
                    title = data.articleTitle,
                    content = "",
                    publishedAt = Instant.ofEpochSecond(data.createdAt),
                    authorId = data.authorId,
                    authorName = data.authorName,
                    rawNostrEventJson = data.raw,
                    isBookmarked = false,
                    stats = EventStatsUi(),
                    authorAvatarCdnImage = data.authorAvatarCdnImage,
                    imageCdnImage = data.articleImageCdnImage,
                    readingTimeInMinutes = data.articleReadingTimeInMinutes,
                ),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                hasBorder = hasBorder,
                onClick = {
                    noteCallbacks.onArticleClick?.invoke(data.naddr)
                },
            )
        }
    }
}

package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.Instant
import net.primal.android.core.compose.attachment.model.asNoteAttachmentUi
import net.primal.android.core.compose.feed.model.EventStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.NoteNostrUriUi
import net.primal.android.core.compose.feed.model.asNoteNostrUriUi
import net.primal.android.core.compose.feed.note.events.InvoicePayClickEvent
import net.primal.android.core.compose.feed.note.events.MediaClickEvent
import net.primal.android.core.utils.parseHashtags

@Composable
fun ReferencedNotesColumn(
    modifier: Modifier = Modifier,
    postResources: List<NoteNostrUriUi>,
    expanded: Boolean,
    containerColor: Color,
    onPostClick: ((noteId: String) -> Unit)? = null,
    onArticleClick: ((naddr: String) -> Unit)? = null,
    onMediaClick: ((MediaClickEvent) -> Unit)? = null,
    onPayInvoiceClick: ((InvoicePayClickEvent) -> Unit)? = null,
) {
    val displayableNotes = if (postResources.isNotEmpty()) {
        if (expanded) postResources else postResources.subList(0, 1)
    } else {
        emptyList()
    }

    Column(modifier = modifier) {
        displayableNotes.forEach { nostrResourceUi ->
            val data = nostrResourceUi.referencedPost
            checkNotNull(data)
            ReferencedNoteCard(
                modifier = Modifier.padding(vertical = 4.dp),
                data = FeedPostUi(
                    postId = data.postId,
                    repostId = null,
                    repostAuthorId = null,
                    repostAuthorName = null,
                    authorId = data.authorId,
                    authorName = data.authorName,
                    authorHandle = data.authorName,
                    authorInternetIdentifier = data.authorInternetIdentifier,
                    authorAvatarCdnImage = data.authorAvatarCdnImage,
                    attachments = data.attachments.map { it.asNoteAttachmentUi() },
                    nostrUris = data.nostrUris.map { it.asNoteNostrUriUi() },
                    timestamp = Instant.ofEpochSecond(data.createdAt),
                    content = data.content,
                    stats = EventStatsUi(),
                    hashtags = data.content.parseHashtags(),
                    rawNostrEventJson = "",
                    replyToAuthorHandle = null,
                ),
                onPostClick = onPostClick,
                onArticleClick = onArticleClick,
                onMediaClick = onMediaClick,
                onPayInvoiceClick = onPayInvoiceClick,
                colors = CardDefaults.cardColors(containerColor = containerColor),
            )
        }
    }
}

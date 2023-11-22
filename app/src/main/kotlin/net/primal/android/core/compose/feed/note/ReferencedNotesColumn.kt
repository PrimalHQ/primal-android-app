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
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.NoteNostrUriUi
import net.primal.android.core.compose.feed.model.asNoteNostrUriUi
import net.primal.android.core.utils.parseHashtags

@Composable
fun ReferencedNotesColumn(
    postResources: List<NoteNostrUriUi>,
    expanded: Boolean,
    containerColor: Color,
    onPostClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
) {
    val displayableNotes = if (postResources.isNotEmpty()) {
        if (expanded) postResources else postResources.subList(0, 1)
    } else {
        emptyList()
    }

    Column {
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
                    stats = FeedPostStatsUi(),
                    hashtags = data.content.parseHashtags(),
                    rawNostrEventJson = "",
                ),
                onPostClick = onPostClick,
                onMediaClick = onMediaClick,
                colors = CardDefaults.cardColors(containerColor = containerColor),
            )
        }
    }
}

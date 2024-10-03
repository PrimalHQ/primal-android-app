package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.Instant
import net.primal.android.core.compose.attachment.model.asNoteAttachmentUi
import net.primal.android.core.utils.parseHashtags
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.NoteNostrUriUi
import net.primal.android.notes.feed.model.asNoteNostrUriUi
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks

@Composable
fun ReferencedNotesColumn(
    modifier: Modifier = Modifier,
    postResources: List<NoteNostrUriUi>,
    expanded: Boolean,
    containerColor: Color,
    noteCallbacks: NoteCallbacks,
) {
    val displayableNotes = if (postResources.isNotEmpty()) {
        if (expanded) postResources else postResources.subList(0, 1)
    } else {
        emptyList()
    }

    Column(modifier = modifier) {
        displayableNotes.forEach { nostrResourceUi ->
            val data = nostrResourceUi.referencedNote
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
                colors = CardDefaults.cardColors(containerColor = containerColor),
                noteCallbacks = noteCallbacks,
            )
        }
    }
}

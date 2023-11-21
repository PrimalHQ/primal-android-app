package net.primal.android.core.compose.feed.model

import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.messages.chat.model.ChatMessageUi

data class NoteContentUi(
    val noteId: String,
    val content: String,
    val attachments: List<NoteAttachmentUi> = emptyList(),
    val nostrUris: List<NoteNostrUriUi> = emptyList(),
    val hashtags: List<String> = emptyList(),
)

fun FeedPostUi.toNoteContentUi() =
    NoteContentUi(
        noteId = this.postId,
        content = this.content,
        attachments = this.attachments,
        nostrUris = this.nostrUris,
        hashtags = this.hashtags,
    )

fun ChatMessageUi.toNoteContentUi() =
    NoteContentUi(
        noteId = this.messageId,
        content = this.content,
        attachments = this.attachments,
        nostrUris = this.nostrUris,
        hashtags = this.hashtags,
    )

package net.primal.data.local.dao.threads

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = [
        "noteId",
        "replyNoteId",
    ],
    indices = [
        Index(value = ["noteId"]),
        Index(value = ["replyNoteId"]),
    ],
)
data class NoteConversationCrossRef(
    val noteId: String,
    val replyNoteId: String,
)

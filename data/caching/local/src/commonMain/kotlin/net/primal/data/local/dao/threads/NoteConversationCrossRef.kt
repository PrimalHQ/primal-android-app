package net.primal.data.local.dao.threads

import androidx.room3.Entity
import androidx.room3.Index

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

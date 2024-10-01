package net.primal.android.attachments.db

import androidx.room.Embedded
import androidx.room.Entity
import kotlinx.serialization.Serializable
import net.primal.android.notes.db.ReferencedArticle
import net.primal.android.notes.db.ReferencedNote
import net.primal.android.notes.db.ReferencedUser

@Entity(
    primaryKeys = ["noteId", "uri"],
)
@Serializable
data class NoteNostrUri(
    val noteId: String,
    val uri: String,
    @Embedded(prefix = "refNote_") val referencedNote: ReferencedNote? = null,
    @Embedded(prefix = "refArticle_") val referencedArticle: ReferencedArticle? = null,
    @Embedded(prefix = "refUser_") val referencedUser: ReferencedUser? = null,
)

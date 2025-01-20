package net.primal.android.attachments.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import kotlinx.serialization.Serializable
import net.primal.android.attachments.domain.NostrUriType
import net.primal.android.notes.db.ReferencedArticle
import net.primal.android.notes.db.ReferencedHighlight
import net.primal.android.notes.db.ReferencedNote
import net.primal.android.notes.db.ReferencedUser
import net.primal.android.notes.db.ReferencedZap

@Entity(
    primaryKeys = ["noteId", "uri"],
)
@Serializable
data class NoteNostrUri(
    val noteId: String,
    val uri: String,
    val type: NostrUriType,
    @ColumnInfo("refEvent_alt") val referencedEventAlt: String? = null,
    @Embedded(prefix = "refHighlight_") val referencedHighlight: ReferencedHighlight? = null,
    @Embedded(prefix = "refNote_") val referencedNote: ReferencedNote? = null,
    @Embedded(prefix = "refArticle_") val referencedArticle: ReferencedArticle? = null,
    @Embedded(prefix = "refUser_") val referencedUser: ReferencedUser? = null,
    @Embedded(prefix = "refZap_") val referencedZap: ReferencedZap? = null,
)

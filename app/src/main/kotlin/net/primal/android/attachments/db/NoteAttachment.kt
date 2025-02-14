package net.primal.android.attachments.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import net.primal.android.attachments.domain.CdnResourceVariant
import net.primal.android.attachments.domain.NoteAttachmentType

@Entity(indices = [Index(value = ["eventId", "url"], unique = true)])
@Serializable
data class NoteAttachment(
    @PrimaryKey(autoGenerate = true)
    val position: Int = 0,
    val eventId: String,
    val url: String,
    val type: NoteAttachmentType,
    val mimeType: String? = null,
    val variants: List<CdnResourceVariant>? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnail: String? = null,
    val authorAvatarUrl: String? = null,
)

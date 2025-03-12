package net.primal.db.events

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(indices = [Index(value = ["eventId", "uri"], unique = true)])
@Serializable
data class EventUriNostr(
    @PrimaryKey(autoGenerate = true)
    val position: Int = 0,
    val eventId: String,
    val uri: String,
    val type: EventUriNostrType,
    val referencedEventAlt: String? = null,
    @Embedded(prefix = "refHighlight_") val referencedHighlight: ReferencedHighlight? = null,
    @Embedded(prefix = "refNote_") val referencedNote: ReferencedNote? = null,
    @Embedded(prefix = "refArticle_") val referencedArticle: ReferencedArticle? = null,
    @Embedded(prefix = "refUser_") val referencedUser: ReferencedUser? = null,
    @Embedded(prefix = "refZap_") val referencedZap: ReferencedZap? = null,
)

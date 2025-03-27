package net.primal.android.events.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import net.primal.domain.EventUriNostrType
import net.primal.domain.ReferencedArticle
import net.primal.domain.ReferencedHighlight
import net.primal.domain.ReferencedNote
import net.primal.domain.ReferencedUser
import net.primal.domain.ReferencedZap

@Entity(indices = [Index(value = ["eventId", "uri"], unique = true)])
@Serializable
data class EventUriNostr(
    @PrimaryKey(autoGenerate = true)
    val position: Int = 0,
    val eventId: String,
    val uri: String,
    val type: EventUriNostrType,
    val referencedEventAlt: String? = null,
    val referencedHighlight: ReferencedHighlight? = null,
    val referencedNote: ReferencedNote? = null,
    val referencedArticle: ReferencedArticle? = null,
    val referencedUser: ReferencedUser? = null,
    val referencedZap: ReferencedZap? = null,
)

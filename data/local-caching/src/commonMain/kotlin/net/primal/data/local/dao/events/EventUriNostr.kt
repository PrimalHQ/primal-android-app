package net.primal.data.local.dao.events

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import net.primal.domain.links.EventUriNostrType
import net.primal.domain.links.ReferencedArticle
import net.primal.domain.links.ReferencedHighlight
import net.primal.domain.links.ReferencedNote
import net.primal.domain.links.ReferencedUser
import net.primal.domain.links.ReferencedZap

@Entity(indices = [Index(value = ["eventId", "uri"], unique = true)])
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

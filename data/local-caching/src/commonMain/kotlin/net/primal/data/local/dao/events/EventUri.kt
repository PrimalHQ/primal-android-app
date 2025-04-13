package net.primal.data.local.dao.events

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import net.primal.domain.links.CdnResourceVariant
import net.primal.domain.links.EventUriType

@Entity(indices = [Index(value = ["eventId", "url"], unique = true)])
data class EventUri(
    @PrimaryKey(autoGenerate = true)
    val position: Int = 0,
    val eventId: String,
    val url: String,
    val type: EventUriType,
    val mimeType: String? = null,
    val variants: List<CdnResourceVariant>? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnail: String? = null,
    val authorAvatarUrl: String? = null,
)

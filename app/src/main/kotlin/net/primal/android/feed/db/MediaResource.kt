package net.primal.android.feed.db

import androidx.room.Entity
import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.primal.PrimalResourceVariant

@Entity(
    primaryKeys = ["eventId", "url"]
)
@Serializable
data class MediaResource(
    val eventId: String,
    val url: String,
    val contentType: String? = null,
    val variants: List<PrimalResourceVariant>? = null,
)

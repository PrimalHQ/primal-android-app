package net.primal.android.feed.db

import androidx.room.Entity
import net.primal.android.nostr.model.primal.PrimalResourceVariant
import net.primal.android.profile.db.ProfileMetadata

@Entity(
    primaryKeys = ["eventId", "entity"]
)
data class Nip19Entity(
    val eventId: String,
    val entity: String,
    val profile: ProfileMetadata? = null,
)

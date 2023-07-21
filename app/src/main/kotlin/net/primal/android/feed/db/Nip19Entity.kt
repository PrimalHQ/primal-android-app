package net.primal.android.feed.db

import androidx.room.Entity
import androidx.room.Relation
import net.primal.android.nostr.model.primal.PrimalResourceVariant
import net.primal.android.profile.db.ProfileMetadata

@Entity(
    primaryKeys = ["eventId", "link"]
)
data class Nip19Entity(
    val eventId: String,
    val link: String,
    val profileId: String?,
    val displayName: String?,
)

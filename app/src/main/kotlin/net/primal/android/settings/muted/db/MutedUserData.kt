package net.primal.android.settings.muted.db

import androidx.room.Entity

@Entity(primaryKeys = ["userId", "ownerId"])
data class MutedUserData(
    val userId: String,
    val ownerId: String,
    val userMetadataEventId: String? = null,
)

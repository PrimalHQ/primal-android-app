package net.primal.data.local.dao.profiles

import androidx.room.Entity

@Entity(primaryKeys = ["userId", "ownerId"])
data class MutedUserData(
    val userId: String,
    val ownerId: String,
    val userMetadataEventId: String? = null,
)

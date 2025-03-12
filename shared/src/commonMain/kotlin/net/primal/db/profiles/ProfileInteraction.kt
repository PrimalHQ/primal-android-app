package net.primal.db.profiles

import androidx.room.Entity

@Entity(primaryKeys = ["profileId", "ownerId"])
data class ProfileInteraction(
    val profileId: String,
    val ownerId: String,
    val lastInteractionAt: Long,
)

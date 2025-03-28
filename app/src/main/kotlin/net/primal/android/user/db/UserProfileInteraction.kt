package net.primal.android.user.db

import androidx.room.Entity

@Entity(primaryKeys = ["profileId", "ownerId"])
data class UserProfileInteraction(
    val profileId: String,
    val ownerId: String,
    val lastInteractionAt: Long,
)

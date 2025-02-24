package net.primal.android.profile.db

import androidx.room.Entity

@Entity(primaryKeys = ["profileId", "ownerId"])
data class ProfileInteraction(
    val profileId: String,
    val ownerId: String,
    val lastInteractionAt: Long,
)

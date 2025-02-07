package net.primal.android.profile.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(primaryKeys = ["profileId", "ownerId"])
data class ProfileInteraction(
    val profileId: String,
    val ownerId: String,
    val lastInteractionAt: Long,
)

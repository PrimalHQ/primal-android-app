package net.primal.android.profile.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ProfileInteraction(
    @PrimaryKey
    val profileId: String,
    val lastInteractionAt: Long,
)

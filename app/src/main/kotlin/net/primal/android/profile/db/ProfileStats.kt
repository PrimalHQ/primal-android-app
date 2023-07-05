package net.primal.android.profile.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ProfileStats(
    @PrimaryKey
    val profileId: String,
    val following: Int,
    val followers: Int,
    val notes: Int,
)

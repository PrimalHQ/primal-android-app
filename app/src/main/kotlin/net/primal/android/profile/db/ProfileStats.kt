package net.primal.android.profile.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ProfileStats(
    @PrimaryKey
    val profileId: String,
    val joinedAt: Long,
    val following: Int,
    val followers: Int,
    val notesCount: Int,
    val repliesCount: Int,
    val relaysCount: Int,
    val totalReceivedZaps: Long,
    val totalReceivedSats: Long,
)

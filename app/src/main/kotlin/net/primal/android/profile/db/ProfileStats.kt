package net.primal.android.profile.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ProfileStats(
    @PrimaryKey
    val profileId: String,
    val following: Int? = null,
    val followers: Int? = null,
    val notesCount: Int? = null,
    val repliesCount: Int? = null,
    val relaysCount: Int? = null,
    val totalReceivedZaps: Long? = null,
    val totalReceivedSats: Long? = null,
    val joinedAt: Long? = null,
)

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
    val readsCount: Int? = null,
    val mediaCount: Int? = null,
    val repliesCount: Int? = null,
    val relaysCount: Int? = null,
    val totalReceivedZaps: Long? = null,
    val contentZapCount: Int? = null,
    val totalReceivedSats: Long? = null,
    val joinedAt: Long? = null,
)

package net.primal.android.core.compose.profile.model

import net.primal.android.profile.db.ProfileStats

data class ProfileStatsUi(
    val followingCount: Int? = null,
    val followersCount: Int? = null,
    val notesCount: Int? = null,
    val repliesCount: Int? = null,
    val readsCount: Int? = null,
    val mediaCount: Int? = null,
)

fun ProfileStats.asProfileStatsUi() =
    ProfileStatsUi(
        followingCount = this.following,
        followersCount = this.followers,
        notesCount = this.notesCount,
        repliesCount = this.repliesCount,
        readsCount = this.readsCount,
        mediaCount = this.mediaCount,
    )

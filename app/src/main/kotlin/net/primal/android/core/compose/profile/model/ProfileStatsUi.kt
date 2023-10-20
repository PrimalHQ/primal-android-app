package net.primal.android.core.compose.profile.model

import net.primal.android.profile.db.ProfileStats

data class ProfileStatsUi(
    val followingCount: Int?,
    val followersCount: Int?,
    val notesCount: Int?,
)

fun ProfileStats.asProfileStatsUi() =
    ProfileStatsUi(
        followingCount = this.following,
        followersCount = this.followers,
        notesCount = this.notes,
    )

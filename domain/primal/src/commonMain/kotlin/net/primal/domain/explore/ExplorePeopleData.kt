package net.primal.domain.explore

import net.primal.domain.profile.ProfileData

data class ExplorePeopleData(
    val profile: ProfileData,
    val userScore: Float,
    val userFollowersCount: Int,
    val followersIncrease: Int,
    val verifiedFollowersCount: Int,
)

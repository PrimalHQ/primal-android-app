package net.primal.domain

import net.primal.domain.model.ProfileData

data class ExplorePeopleData(
    val profile: ProfileData,
    val userScore: Float,
    val userFollowersCount: Int,
    val followersIncrease: Int,
    val verifiedFollowersCount: Int,
)

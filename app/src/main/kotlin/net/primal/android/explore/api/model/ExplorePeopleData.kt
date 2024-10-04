package net.primal.android.explore.api.model

import net.primal.android.core.compose.profile.model.ProfileDetailsUi

data class ExplorePeopleData(
    val profile: ProfileDetailsUi,
    val userScore: Float,
    val userFollowersCount: Int,
    val followersIncrease: Int,
    val verifiedFollowersCount: Int,
)

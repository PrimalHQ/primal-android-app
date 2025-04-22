package net.primal.domain.common

import net.primal.domain.profile.ProfileData

data class UserProfileSearchItem(
    val metadata: ProfileData,
    val score: Float? = null,
    val followersCount: Int? = null,
)

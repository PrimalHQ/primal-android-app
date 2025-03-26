package net.primal.domain

import net.primal.domain.model.ProfileData

data class UserProfileSearchItem(
    val metadata: ProfileData,
    val score: Float? = null,
    val followersCount: Int? = null,
)

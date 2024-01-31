package net.primal.android.explore.domain

import net.primal.android.profile.db.ProfileData

data class UserProfileSearchItem(
    val metadata: ProfileData,
    val score: Float? = null,
    val followersCount: Int? = null,
)

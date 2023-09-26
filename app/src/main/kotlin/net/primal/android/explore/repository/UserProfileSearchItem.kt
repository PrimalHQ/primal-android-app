package net.primal.android.explore.repository

import net.primal.android.profile.db.ProfileData

data class UserProfileSearchItem(
    val metadata: ProfileData,
    val score: Float?,
)

package net.primal.android.explore.repository

import net.primal.android.profile.db.ProfileMetadata

data class UserProfileSearchItem(
    val metadata: ProfileMetadata,
    val score: Float?,
)

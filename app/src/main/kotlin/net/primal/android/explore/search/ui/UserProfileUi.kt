package net.primal.android.explore.search.ui

data class UserProfileUi(
    val profileId: String,
    val displayName: String,
    val internetIdentifier: String? = null,
    val avatarUrl: String? = null,
    val followersCount: Int? = null,
)

package net.primal.android.explore.search.ui

import net.primal.android.attachments.domain.CdnResourceVariant

data class UserProfileUi(
    val profileId: String,
    val displayName: String,
    val internetIdentifier: String? = null,
    val avatarUrl: String? = null,
    val avatarVariants: List<CdnResourceVariant> = emptyList(),
    val followersCount: Int? = null,
)

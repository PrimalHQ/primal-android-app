package net.primal.android.explore.search.ui

import net.primal.android.attachments.domain.CdnImage

data class UserProfileUi(
    val profileId: String,
    val displayName: String,
    val internetIdentifier: String? = null,
    val avatarCdnImage: CdnImage? = null,
    val followersCount: Int? = null,
)

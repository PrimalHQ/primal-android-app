package net.primal.android.profile.domain

import android.net.Uri

data class ProfileMetadata(
    val displayName: String,
    val username: String,
    val website: String? = null,
    val about: String? = null,
    val lightningAddress: String? = null,
    val nostrVerification: String? = null,
    val localPictureUri: Uri? = null,
    val localBannerUri: Uri? = null,
    val remotePictureUrl: String? = null,
    val remoteBannerUrl: String? = null,
)

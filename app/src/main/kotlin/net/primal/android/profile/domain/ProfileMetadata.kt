package net.primal.android.profile.domain

import android.net.Uri

data class ProfileMetadata(
    val displayName: String,
    val handle: String,
    val website: String? = null,
    val about: String? = null,
    val lightningAddress: String? = null,
    val nostrVerification: String? = null,
    val picture: Uri? = null,
    val banner: Uri? = null,
)

package net.primal.android.core.compose.profile.model

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.profile.db.ProfileData

data class ProfileDetailsUi(
    val pubkey: String,
    val authorDisplayName: String,
    val userDisplayName: String,
    val coverCdnImage: CdnImage?,
    val avatarCdnImage: CdnImage?,
    val internetIdentifier: String?,
    val lightningAddress: String?,
    val about: String?,
    val aboutHashtags: List<String> = emptyList(),
    val aboutUris: List<String> = emptyList(),
    val website: String?,
)

fun ProfileData.asProfileDetailsUi() =
    ProfileDetailsUi(
        pubkey = this.ownerId,
        authorDisplayName = this.authorNameUiFriendly(),
        userDisplayName = this.usernameUiFriendly(),
        coverCdnImage = this.bannerCdnImage,
        avatarCdnImage = this.avatarCdnImage,
        internetIdentifier = this.internetIdentifier,
        lightningAddress = this.lightningAddress,
        about = this.about,
        aboutHashtags = this.aboutHashtags,
        aboutUris = this.aboutUris,
        website = this.website,
    )

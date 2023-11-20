package net.primal.android.core.compose.profile.model

import net.primal.android.attachments.domain.CdnResourceVariant
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.profile.db.ProfileData

data class ProfileDetailsUi(
    val pubkey: String,
    val authorDisplayName: String,
    val userDisplayName: String,
    val coverUrl: String?,
    val coverVariants: List<CdnResourceVariant>? = null,
    val avatarUrl: String?,
    val avatarVariants: List<CdnResourceVariant>? = null,
    val internetIdentifier: String?,
    val about: String?,
    val website: String?,
)

fun ProfileData.asProfileDetailsUi() =
    ProfileDetailsUi(
        pubkey = this.ownerId,
        authorDisplayName = this.authorNameUiFriendly(),
        userDisplayName = this.usernameUiFriendly(),
        coverUrl = this.bannerUrl,
        coverVariants = this.bannerVariants,
        avatarUrl = this.avatarUrl,
        avatarVariants = this.avatarVariants,
        internetIdentifier = this.internetIdentifier,
        about = this.about,
        website = this.website,
    )

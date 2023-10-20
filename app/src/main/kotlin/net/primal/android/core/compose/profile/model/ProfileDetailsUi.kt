package net.primal.android.core.compose.profile.model

import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.profile.db.ProfileData

data class ProfileDetailsUi(
    val pubkey: String,
    val authorDisplayName: String,
    val userDisplayName: String,
    val coverUrl: String?,
    val avatarUrl: String?,
    val internetIdentifier: String?,
    val about: String?,
    val website: String?,
)

fun ProfileData.asProfileDetailsUi() =
    ProfileDetailsUi(
        pubkey = this.ownerId,
        authorDisplayName = this.authorNameUiFriendly(),
        userDisplayName = this.usernameUiFriendly(),
        coverUrl = this.banner,
        avatarUrl = this.picture,
        internetIdentifier = this.internetIdentifier,
        about = this.about,
        website = this.website,
    )

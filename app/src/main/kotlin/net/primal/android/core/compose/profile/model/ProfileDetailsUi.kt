package net.primal.android.core.compose.profile.model

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.premium.legend.asLegendaryCustomization
import net.primal.android.profile.db.ProfileData

data class ProfileDetailsUi(
    val pubkey: String,
    val authorDisplayName: String,
    val userDisplayName: String,
    val coverCdnImage: CdnImage? = null,
    val avatarCdnImage: CdnImage? = null,
    val internetIdentifier: String? = null,
    val lightningAddress: String? = null,
    val about: String? = null,
    val aboutHashtags: List<String> = emptyList(),
    val aboutUris: List<String> = emptyList(),
    val website: String? = null,
    val primalName: String? = null,
    val lnUrlDecoded: String? = null,
    val legendaryCustomization: LegendaryCustomization? = null,
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
        primalName = this.primalName,
        lnUrlDecoded = this.lnUrlDecoded,
        legendaryCustomization = this.primalLegendProfile?.asLegendaryCustomization(),
    )

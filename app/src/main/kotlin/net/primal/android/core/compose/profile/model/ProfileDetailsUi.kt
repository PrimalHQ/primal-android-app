package net.primal.android.core.compose.profile.model

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.premium.legend.asLegendaryCustomization
import net.primal.android.profile.db.ProfileData
import net.primal.android.profile.details.ui.model.PremiumProfileDataUi
import net.primal.android.profile.domain.PrimalPremiumInfo

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
    val premiumDetails: PremiumProfileDataUi? = null,
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
        primalName = this.primalPremiumInfo?.primalName,
        lnUrlDecoded = this.lnUrlDecoded,
        premiumDetails = this.primalPremiumInfo?.asPremiumProfileDataUi(),
    )

private fun PrimalPremiumInfo.asPremiumProfileDataUi() =
    PremiumProfileDataUi(
        primalName = this.primalName,
        cohort1 = this.cohort1,
        cohort2 = this.cohort2,
        tier = this.tier,
        expiresAt = this.expiresAt,
        legendaryCustomization = this.legendProfile?.asLegendaryCustomization(),
    )

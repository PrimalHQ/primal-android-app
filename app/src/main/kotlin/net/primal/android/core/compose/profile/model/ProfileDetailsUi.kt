package net.primal.android.core.compose.profile.model

import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.profile.details.ui.model.PremiumProfileDataUi
import net.primal.domain.CdnImage
import net.primal.domain.PrimalPremiumInfo
import net.primal.domain.model.ProfileData as ProfileDataDO

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

fun ProfileDataDO.asProfileDetailsUi() =
    ProfileDetailsUi(
        pubkey = this.profileId,
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

fun PrimalPremiumInfo.asPremiumProfileDataUi() =
    PremiumProfileDataUi(
        primalName = this.primalName,
        cohort1 = this.cohort1,
        cohort2 = this.cohort2,
        tier = this.tier,
        legendSince = this.legendSince,
        premiumSince = this.premiumSince,
        expiresAt = this.expiresAt,
        legendaryCustomization = this.legendProfile?.asLegendaryCustomization(),
    )

fun String.asFallbackProfileDetailsUi() =
    ProfileDetailsUi(
        pubkey = this,
        authorDisplayName = this.asEllipsizedNpub(),
        userDisplayName = this.asEllipsizedNpub(),
    )

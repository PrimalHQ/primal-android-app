package net.primal.domain.profile

import net.primal.domain.links.CdnImage
import net.primal.domain.premium.PrimalPremiumInfo

data class ProfileData(
    val profileId: String,
    val metadataEventId: String? = null,
    val createdAt: Long? = null,
    val metadataRawEvent: String? = null,
    val handle: String? = null,
    val displayName: String? = null,
    val internetIdentifier: String? = null,
    val lightningAddress: String? = null,
    val lnUrlDecoded: String? = null,
    val avatarCdnImage: CdnImage? = null,
    val bannerCdnImage: CdnImage? = null,
    val website: String? = null,
    val about: String? = null,
    val aboutUris: List<String> = emptyList(),
    val aboutHashtags: List<String> = emptyList(),
    val primalName: String? = null,
    val primalPremiumInfo: PrimalPremiumInfo? = null,
    val blossoms: List<String> = emptyList(),
)

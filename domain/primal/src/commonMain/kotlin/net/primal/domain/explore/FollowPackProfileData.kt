package net.primal.domain.explore

import net.primal.domain.links.CdnImage
import net.primal.domain.premium.PrimalPremiumInfo

data class FollowPackProfileData(
    val profileId: String,
    val displayName: String,
    val internetIdentifier: String?,
    val followersCount: Int,
    val avatarCdnImage: CdnImage?,
    val primalPremiumInfo: PrimalPremiumInfo?,
)

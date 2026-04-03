package net.primal.domain.explore

import net.primal.domain.links.CdnImage
import net.primal.domain.membership.PrimalPremiumInfo
import net.primal.domain.profile.Nip05VerificationStatus

data class FollowPackProfileData(
    val profileId: String,
    val displayName: String,
    val internetIdentifier: String?,
    val followersCount: Int,
    val avatarCdnImage: CdnImage?,
    val primalPremiumInfo: PrimalPremiumInfo?,
    val nip05Status: Nip05VerificationStatus? = null,
)

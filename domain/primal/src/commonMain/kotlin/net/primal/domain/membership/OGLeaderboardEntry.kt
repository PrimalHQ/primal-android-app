package net.primal.domain.membership

import net.primal.domain.links.CdnImage
import net.primal.domain.profile.Nip05VerificationStatus

data class OGLeaderboardEntry(
    val index: Int,
    val userId: String,
    val avatarCdnImage: CdnImage?,
    val displayName: String?,
    val internetIdentifier: String?,
    val firstCohort: String?,
    val secondCohort: String?,
    val premiumSince: Long?,
    val nip05Status: Nip05VerificationStatus? = null,
)

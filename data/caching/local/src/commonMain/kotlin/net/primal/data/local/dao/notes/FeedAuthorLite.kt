package net.primal.data.local.dao.notes

import net.primal.domain.links.CdnImage
import net.primal.domain.membership.PrimalPremiumInfo

/**
 * Column-subset of [net.primal.data.local.dao.profiles.ProfileData] for the
 * feed `author` relation.
 */
data class FeedAuthorLite(
    val ownerId: String,
    val displayName: String? = null,
    val handle: String? = null,
    val internetIdentifier: String? = null,
    val avatarCdnImage: CdnImage? = null,
    val primalPremiumInfo: PrimalPremiumInfo? = null,
    val blossoms: List<String> = emptyList(),
)

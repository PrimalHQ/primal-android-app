package net.primal.domain.posts

import net.primal.domain.links.CdnImage
import net.primal.domain.premium.PrimalLegendProfile

data class FeedPostAuthor(
    val authorId: String,
    val handle: String,
    val displayName: String,
    val rawNostrEvent: String? = null,
    val internetIdentifier: String? = null,
    val avatarCdnImage: CdnImage? = null,
    val legendProfile: PrimalLegendProfile? = null,
    val blossomServers: List<String> = emptyList(),
)

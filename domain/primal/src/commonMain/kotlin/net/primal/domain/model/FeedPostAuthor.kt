package net.primal.domain.model

import net.primal.domain.CdnImage
import net.primal.domain.PrimalLegendProfile

data class FeedPostAuthor(
    val authorId: String,
    val handle: String,
    val displayName: String,
    val internetIdentifier: String? = null,
    val avatarCdnImage: CdnImage? = null,
    val legendProfile: PrimalLegendProfile? = null,
    val blossomServers: List<String> = emptyList(),
)

package net.primal.domain.model

import net.primal.domain.FeedSpecKind

data class PrimalFeed(
    val ownerId: String,
    val spec: String,
    val specKind: FeedSpecKind,
    val feedKind: String,
    val title: String,
    val description: String,
    val enabled: Boolean = true,
    val position: Int = 0,
)

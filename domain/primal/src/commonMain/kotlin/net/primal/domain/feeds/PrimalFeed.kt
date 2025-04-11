package net.primal.domain.feeds

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

package net.primal.android.core.compose.feed.model

import net.primal.android.nostr.model.primal.PrimalResourceVariant

data class FeedPostResource(
    val url: String,
    val mimeType: String? = null,
    val variants: List<PrimalResourceVariant> = emptyList(),
)
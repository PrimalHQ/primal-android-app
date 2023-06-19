package net.primal.android.feed.shared.model

import net.primal.android.nostr.model.primal.PrimalResourceVariant

data class FeedPostResource(
    val url: String,
    val mimeType: String? = null,
    val variants: List<PrimalResourceVariant> = emptyList(),
)
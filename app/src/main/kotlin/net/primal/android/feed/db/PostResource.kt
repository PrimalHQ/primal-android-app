package net.primal.android.feed.db

import androidx.room.Entity
import net.primal.android.nostr.model.primal.PrimalResourceVariant

@Entity(
    primaryKeys = ["postId", "url"]
)
data class PostResource(
    val postId: String,
    val url: String,
    val contentType: String? = null,
    val variants: List<PrimalResourceVariant>? = null,
)

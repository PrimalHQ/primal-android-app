package net.primal.android.feed.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.android.nostr.model.primal.PrimalResourceVariant

@Entity
data class PostResource(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val postId: String,
    val mimeType: String,
    val url: String,
    val variants: List<PrimalResourceVariant>,
)

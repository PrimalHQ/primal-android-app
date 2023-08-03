package net.primal.android.feed.db

import androidx.room.Embedded
import androidx.room.Entity
import kotlinx.serialization.Serializable

@Entity(
    primaryKeys = ["postId", "uri"]
)
@Serializable
data class NostrResource(
    val postId: String,
    val uri: String,
    @Embedded(prefix = "refPost_") val referencedPost: ReferencedPost? = null,
    @Embedded(prefix = "refUser_") val referencedUser: ReferencedUser? = null,
)

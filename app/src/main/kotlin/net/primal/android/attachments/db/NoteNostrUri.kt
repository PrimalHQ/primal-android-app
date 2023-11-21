package net.primal.android.attachments.db

import androidx.room.Embedded
import androidx.room.Entity
import kotlinx.serialization.Serializable
import net.primal.android.feed.db.ReferencedPost
import net.primal.android.feed.db.ReferencedUser

@Entity(
    primaryKeys = ["postId", "uri"],
)
@Serializable
data class NoteNostrUri(
    val postId: String,
    val uri: String,
    @Embedded(prefix = "refPost_") val referencedPost: ReferencedPost? = null,
    @Embedded(prefix = "refUser_") val referencedUser: ReferencedUser? = null,
)

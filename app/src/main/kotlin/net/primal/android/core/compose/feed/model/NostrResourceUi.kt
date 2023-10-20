package net.primal.android.core.compose.feed.model

import net.primal.android.feed.db.NostrResource
import net.primal.android.feed.db.ReferencedPost
import net.primal.android.feed.db.ReferencedUser

data class NostrResourceUi(
    val uri: String,
    val referencedUser: ReferencedUser?,
    val referencedPost: ReferencedPost?,
)

fun NostrResource.asNostrResourceUi() = NostrResourceUi(
    uri = this.uri,
    referencedPost = this.referencedPost,
    referencedUser = this.referencedUser,
)

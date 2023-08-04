package net.primal.android.core.compose.feed.model

import net.primal.android.feed.db.ReferencedPost
import net.primal.android.feed.db.ReferencedUser

data class NostrResourceUi(
    val uri: String,
    val referencedUser: ReferencedUser?,
    val referencedPost: ReferencedPost?,
)

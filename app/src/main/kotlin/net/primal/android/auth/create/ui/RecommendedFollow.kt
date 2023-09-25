package net.primal.android.auth.create.ui

import net.primal.android.nostr.model.content.ContentMetadata

data class RecommendedFollow(
    val pubkey: String,
    val isCurrentUserFollowing: Boolean = false,
    val groupName: String,
    val content: ContentMetadata,
)

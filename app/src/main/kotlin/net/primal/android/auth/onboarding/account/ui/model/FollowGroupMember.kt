package net.primal.android.auth.onboarding.account.ui.model

import net.primal.domain.nostr.ContentMetadata

data class FollowGroupMember(
    val name: String,
    val userId: String,
    val followed: Boolean = true,
    val metadata: ContentMetadata? = null,
)

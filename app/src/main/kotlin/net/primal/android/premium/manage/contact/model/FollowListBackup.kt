package net.primal.android.premium.manage.contact.model

import net.primal.domain.nostr.NostrEvent

data class FollowListBackup(
    val event: NostrEvent,
    val timestamp: Long,
    val followsCount: Int,
)

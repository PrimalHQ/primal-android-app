package net.primal.data.remote.api.users.model

import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class BookmarksResponse(
    val bookmarksListEvent: NostrEvent? = null,
)

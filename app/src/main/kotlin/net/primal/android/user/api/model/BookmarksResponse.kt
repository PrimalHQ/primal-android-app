package net.primal.android.user.api.model

import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class BookmarksResponse(
    val bookmarksListEvent: NostrEvent? = null,
)

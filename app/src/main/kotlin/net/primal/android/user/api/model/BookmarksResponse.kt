package net.primal.android.user.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent

@Serializable
data class BookmarksResponse(
    val bookmarksListEvent: NostrEvent? = null,
)

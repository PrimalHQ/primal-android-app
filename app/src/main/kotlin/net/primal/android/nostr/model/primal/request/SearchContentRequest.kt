package net.primal.android.nostr.model.primal.request

import kotlinx.serialization.Serializable

@Serializable
data class SearchContentRequest(
    val query: String,
    val limit: Int = 1000,
)

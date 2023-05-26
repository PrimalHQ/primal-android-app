package net.primal.android.nostr.model.primal.response

import kotlinx.serialization.Serializable

@Serializable
data class AppSettingsResponse(
    val feeds: List<FeedData> = emptyList(),
)
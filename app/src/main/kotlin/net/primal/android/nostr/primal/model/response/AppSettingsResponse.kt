package net.primal.android.nostr.primal.model.response

import kotlinx.serialization.Serializable

@Serializable
data class AppSettingsResponse(
    val feeds: List<FeedData>,
)
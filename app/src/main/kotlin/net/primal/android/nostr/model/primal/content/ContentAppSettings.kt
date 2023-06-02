package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.Serializable

@Serializable
data class ContentAppSettings(
    val feeds: List<ContentFeedData> = emptyList(),
)

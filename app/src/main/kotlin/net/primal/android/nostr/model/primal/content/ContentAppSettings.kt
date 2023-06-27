package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.Serializable

@Serializable
data class ContentAppSettings(
    val theme: String? = null,
    val feeds: List<ContentFeedData> = emptyList(),
    val defaultZapAmount: Long? = null,
    val zapOptions: List<Long> = emptyList(),
)

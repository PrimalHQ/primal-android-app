package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ContentAppSettings(
    val description: String? = null,
    val theme: String? = null,
    val feeds: List<ContentFeedData> = emptyList(),
    val notifications: JsonObject,
    val defaultZapAmount: Long? = null,
    val zapOptions: List<Long> = emptyList(),
)

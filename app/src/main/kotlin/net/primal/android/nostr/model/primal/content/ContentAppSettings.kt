package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ContentAppSettings(
    val description: String? = null,
    val feeds: List<ContentFeedData> = emptyList(),
    val notifications: JsonObject,
    val defaultZapAmount: ULong? = null,
    val zapOptions: List<ULong> = emptyList(),
)

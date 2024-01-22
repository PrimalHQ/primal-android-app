package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ContentAppSettings(
    val description: String? = null,
    val feeds: List<ContentFeedData> = emptyList(),
    val notifications: JsonObject,
    @Deprecated("Replaced with zapDefault.")
    val defaultZapAmount: ULong? = null,
    @Deprecated("Replaced with zapsConfig.")
    val zapOptions: List<ULong> = emptyList(),
    val zapDefault: ContentZapDefault? = null,
    @SerialName("zapConfig") val zapsConfig: List<ContentZapConfigItem> = emptyList(),
)

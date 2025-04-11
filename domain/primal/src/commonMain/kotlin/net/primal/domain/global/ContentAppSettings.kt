package net.primal.domain.global

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import net.primal.domain.notifications.ContentZapConfigItem
import net.primal.domain.notifications.ContentZapDefault

@Serializable
data class ContentAppSettings(
    val description: String? = null,
    val notifications: JsonObject,
    @Deprecated("Replaced with zapDefault.")
    val defaultZapAmount: ULong? = null,
    @Deprecated("Replaced with zapsConfig.")
    val zapOptions: List<ULong> = emptyList(),
    val zapDefault: ContentZapDefault? = null,
    @SerialName("zapConfig") val zapsConfig: List<ContentZapConfigItem> = emptyList(),
)

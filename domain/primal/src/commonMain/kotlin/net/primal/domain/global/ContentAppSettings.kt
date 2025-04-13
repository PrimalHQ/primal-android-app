package net.primal.domain.global

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import net.primal.domain.notifications.ContentZapConfigItem
import net.primal.domain.notifications.ContentZapDefault

@Serializable
data class ContentAppSettings(
    val description: String? = null,
    val notifications: JsonObject,
    val pushNotifications: JsonObject = buildJsonObject {
        put("NEW_FOLLOWS", JsonPrimitive(true))
        put("ZAPS", JsonPrimitive(true))
        put("REACTIONS", JsonPrimitive(true))
        put("REPLIES", JsonPrimitive(true))
        put("REPOSTS", JsonPrimitive(true))
        put("MENTIONS", JsonPrimitive(true))
        put("DIRECT_MESSAGES", JsonPrimitive(true))
        put("WALLET_TRANSACTIONS", JsonPrimitive(true))
    },
    val notificationsAdditional: JsonObject = buildJsonObject {
        put("ignore_events_with_too_many_mentions", JsonPrimitive(true))
        put("only_show_dm_notifications_from_users_i_follow", JsonPrimitive(true))
        put("only_show_reactions_from_users_i_follow", JsonPrimitive(false))
    },
    @Deprecated("Replaced with zapDefault.") val defaultZapAmount: ULong? = null,
    @Deprecated("Replaced with zapsConfig.") val zapOptions: List<ULong> = emptyList(),
    val zapDefault: ContentZapDefault? = null,
    @SerialName("zapConfig") val zapsConfig: List<ContentZapConfigItem> = emptyList(),
)

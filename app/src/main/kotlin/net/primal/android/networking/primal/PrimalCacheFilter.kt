package net.primal.android.networking.primal

import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import net.primal.android.serialization.NostrJson

data class PrimalCacheFilter(
    val primalVerb: PrimalVerb? = null,
    val optionsJson: String? = null,
) {
    fun toPrimalJsonObject() = buildJsonObject {
        put("cache", buildJsonArray {
            add(primalVerb?.identifier)
            if (optionsJson != null) {
                add(NostrJson.decodeFromString(optionsJson))
            }
        })
    }
}

enum class PrimalVerb(val identifier: String) {
    CONTACT_LIST("contact_list"),
    USER_INFOS("user_infos"),
    USER_PROFILE("user_profile"),
    FEED_DIRECTIVE("feed_directive"),
    TRENDING_HASHTAGS_7D("trending_hashtags_7d"),
    RECOMMENDED_USERS("get_recommended_users"),
    GET_APP_SETTINGS("get_app_settings"),
    GET_DEFAULT_APP_SETTINGS("get_default_app_settings"),
    SET_APP_SETTINGS("set_app_settings"),
    THREAD_VIEW("thread_view"),
    USER_SEARCH("user_search"),
    IMPORT_EVENTS("import_events"),
    GET_NOTIFICATIONS("get_notifications"),
    GET_LAST_SEEN_NOTIFICATIONS("get_notifications_seen"),
    SET_LAST_SEEN_NOTIFICATIONS("set_notifications_seen"),
    NEW_NOTIFICATIONS_COUNT("notification_counts_2"),
    UPLOAD("upload"),
    MUTE_LIST("mutelist"),
    GET_DM_CONTACTS("get_directmsg_contacts"),
    GET_DMS("get_directmsgs"),
    MARK_DMS_AS_READ("reset_directmsg_count"),
    NEW_DMS_COUNT("directmsg_count_2"),
}

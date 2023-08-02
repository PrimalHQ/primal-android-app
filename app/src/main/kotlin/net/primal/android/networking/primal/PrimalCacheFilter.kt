package net.primal.android.networking.primal

import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import net.primal.android.serialization.NostrJson

data class PrimalCacheFilter(
    val primalVerb: Verb? = null,
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

enum class Verb(val identifier: String) {
    CONTACT_LIST("contact_list"),
    USER_PROFILE("user_profile"),
    FEED_DIRECTIVE("feed_directive"),
    TRENDING_HASHTAGS_7D("trending_hashtags_7d"),
    GET_APP_SETTINGS("get_app_settings"),
    THREAD_VIEW("thread_view")
}

package net.primal.core.utils.serialization

import kotlinx.serialization.json.Json

inline fun <reified T> Json.decodeFromStringOrNull(string: String?): T? {
    if (string.isNullOrEmpty()) return null

    return try {
        decodeFromString(string)
    } catch (error: IllegalArgumentException) {
        null
    }
}

inline fun <reified T> String?.decodeFromJsonStringOrNull(): T? {
    return CommonJson.decodeFromStringOrNull(this)
}

inline fun <reified T> T.encodeToJsonString(): String {
    return CommonJson.encodeToString(this)
}

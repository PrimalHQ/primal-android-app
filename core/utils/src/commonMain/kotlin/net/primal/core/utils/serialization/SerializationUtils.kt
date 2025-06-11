package net.primal.core.utils.serialization

import kotlinx.serialization.json.Json

inline fun <reified T> Json.decodeFromStringOrNull(string: String?): T? {
    if (string.isNullOrEmpty()) return null

    return try {
        decodeFromString(string)
    } catch (_: Exception) {
        null
    }
}

inline fun <reified T> String?.decodeFromJsonStringOrNull(): T? {
    return CommonJson.decodeFromStringOrNull(this)
}

/**
 * Encodes an object to a JSON string using CommonJson serializer.
 *
 * Note: When working with JsonObject, use `toString()` instead of this function
 * to ensure proper formatting and compatibility with iOS.
 */
inline fun <reified T> T.encodeToJsonString(): String {
    return CommonJson.encodeToString(this)
}

package net.primal.core.utils

import kotlinx.serialization.json.Json

inline fun <reified T> Json.decodeFromStringOrNull(string: String?): T? {
    if (string.isNullOrEmpty()) return null

    return try {
        decodeFromString(string)
    } catch (error: IllegalArgumentException) {
//        Napier.w(error) { "Unable to decode from json string." }
        null
    }
}

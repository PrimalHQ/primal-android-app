package net.primal.wallet.data.remote.serialization

import kotlinx.serialization.json.Json

internal val WalletJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * Encodes an object to a JSON string using CommonJson serializer.
 *
 * Note: When working with JsonObject, use `toString()` instead of this function
 * to ensure proper formatting and compatibility with iOS.
 */
internal inline fun <reified T> T.encodeToWalletJsonString(): String {
    return WalletJson.encodeToString(this)
}

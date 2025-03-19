package net.primal.core.networking.serialization

import kotlinx.serialization.json.Json

internal val SocketsJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

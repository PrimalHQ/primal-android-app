package net.primal.core.networking.serialization

import kotlinx.serialization.json.Json

internal val NetworkingJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

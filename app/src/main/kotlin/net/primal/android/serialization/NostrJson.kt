package net.primal.android.serialization

import kotlinx.serialization.json.Json

val NostrJson = Json {
    ignoreUnknownKeys = true
}

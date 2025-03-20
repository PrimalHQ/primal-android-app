package net.primal.data.remote.uploader

import kotlinx.serialization.json.Json

internal val UploaderJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

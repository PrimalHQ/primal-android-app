package net.primal.data.remote.api.import.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
data class ImportRequestBody(
    @SerialName("events") val nostrEvents: JsonArray,
)

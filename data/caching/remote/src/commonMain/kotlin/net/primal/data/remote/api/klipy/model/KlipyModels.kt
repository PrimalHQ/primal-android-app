package net.primal.data.remote.api.klipy.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KlipySearchResponse(
    val results: List<KlipyGif> = emptyList(),
    val next: String? = null,
)

@Serializable
data class KlipyGif(
    val id: String,
    @SerialName("content_description") val contentDescription: String = "",
    @SerialName("media_formats") val mediaFormats: Map<String, KlipyMediaFormat> = emptyMap(),
)

@Serializable
data class KlipyMediaFormat(
    val url: String,
    val dims: List<Int> = emptyList(),
    val size: Long = 0,
)

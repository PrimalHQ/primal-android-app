package net.primal.data.remote.api.broadcast.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentEventKindCount(
    val kind: Int,
    @SerialName("cnt") val count: Long,
)

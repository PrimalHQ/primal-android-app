package net.primal.data.remote.api.explore.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewUserFollowStats(
    @SerialName("increase") val increase: Int,
    @SerialName("ratio") val ratio: Float,
    @SerialName("count") val count: Int? = null,
)

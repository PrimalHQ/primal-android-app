package net.primal.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalPollOptionStats(
    val votes: Int = 0,
    @SerialName("satszapped") val satsZapped: Long = 0,
)

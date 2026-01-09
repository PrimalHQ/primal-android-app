package net.primal.domain.premium

import kotlinx.serialization.Serializable

@Serializable
data class BroadcastingStatus(
    val running: Boolean,
    val kinds: List<Int>?,
    val status: String,
    val progress: Float,
)

package net.primal.android.premium.manage.content.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BroadcastingStatus(
    val running: Boolean,
    val kinds: List<Int>?,
    val status: String,
    val progress: Float,
)

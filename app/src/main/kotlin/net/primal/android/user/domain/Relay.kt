package net.primal.android.user.domain

import kotlinx.serialization.Serializable

@Serializable
data class Relay(
    val url: String,
    val read: Boolean,
    val write: Boolean,
)

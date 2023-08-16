package net.primal.android.user.domain

import kotlinx.serialization.Serializable

@Serializable
data class Relay(
    val url: String,
    val read: Boolean,
    val write: Boolean,
)

fun String.toRelay(): Relay = Relay(url = this, read = true, write = true)

@Serializable
data class RelayPermission(
    val read: Boolean,
    val write: Boolean,
)

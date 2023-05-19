package net.primal.android.networking.sockets.model

data class OutgoingMessage<T>(
    val command: String? = null,
    val options: T? = null,
)

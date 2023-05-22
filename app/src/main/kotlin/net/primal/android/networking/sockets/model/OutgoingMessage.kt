package net.primal.android.networking.sockets.model

data class OutgoingMessage<T>(
    val primalVerb: String? = null,
    val options: T? = null,
)

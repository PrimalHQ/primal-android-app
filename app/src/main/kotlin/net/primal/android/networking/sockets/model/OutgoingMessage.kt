package net.primal.android.networking.sockets.model

import java.util.UUID

data class OutgoingMessage<T>(
    val subscriptionId: UUID,
    val command: String? = null,
    val options: T? = null,
)

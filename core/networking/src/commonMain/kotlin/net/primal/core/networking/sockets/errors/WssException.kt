package net.primal.core.networking.sockets.errors

class WssException(
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

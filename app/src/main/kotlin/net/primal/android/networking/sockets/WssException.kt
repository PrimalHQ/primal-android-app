package net.primal.android.networking.sockets

class WssException(
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

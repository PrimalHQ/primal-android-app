package net.primal.domain.error

class NetworkException(message: String?, cause: Throwable?) :
    RuntimeException(message, cause)

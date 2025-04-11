package net.primal.core.networking.primal.api

class UnsuccessfulFileUpload(override val cause: Throwable?) :
    RuntimeException(cause)

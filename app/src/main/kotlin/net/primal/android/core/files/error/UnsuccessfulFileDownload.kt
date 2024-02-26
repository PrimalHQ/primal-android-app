package net.primal.android.core.files.error

class UnsuccessfulFileDownload(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)

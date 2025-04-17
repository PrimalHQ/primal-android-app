package net.primal.domain.nostr.zaps

open class ZapException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)

class ZapRequestException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : ZapException(message = message, cause = cause)

class ZapFailureException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : ZapException(message = message, cause = cause)

package net.primal.data.account.remote.signer.model

import net.primal.domain.nostr.NostrEvent

open class RemoteSignerMethodException(
    open val nostrEvent: NostrEvent,
    override val cause: Throwable? = null,
) : Exception(cause)

data class RemoteSignerMethodParseException(
    val requestId: String?,
    override val nostrEvent: NostrEvent,
    override val cause: Throwable?,
) : RemoteSignerMethodException(nostrEvent, cause)

data class RemoteSignerMethodDecryptException(
    override val nostrEvent: NostrEvent,
) : RemoteSignerMethodException(nostrEvent)

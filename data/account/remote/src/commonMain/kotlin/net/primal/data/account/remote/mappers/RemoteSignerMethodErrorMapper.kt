package net.primal.data.account.remote.mappers

import net.primal.data.account.remote.method.model.RemoteSignerMethodDecryptException
import net.primal.data.account.remote.method.model.RemoteSignerMethodException
import net.primal.data.account.remote.method.model.RemoteSignerMethodParseException
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.domain.nostr.NostrEvent

fun Throwable.mapAsRemoteSignerMethodException(nostrEvent: NostrEvent): RemoteSignerMethodException {
    return when (this) {
        is RemoteSignerMethodException -> this
        else -> RemoteSignerMethodException(nostrEvent = nostrEvent, cause = this)
    }
}

fun RemoteSignerMethodException.mapAsRemoteSignerMethodResponse(): RemoteSignerMethodResponse.Error {
    return when (this) {
        is RemoteSignerMethodDecryptException -> {
            RemoteSignerMethodResponse.Error(
                id = this.nostrEvent.id,
                error = "Failed to decrypt content.",
                clientPubKey = this.nostrEvent.pubKey,
            )
        }

        is RemoteSignerMethodParseException -> {
            val message = this.cause?.message ?: "An error occurred while parsing decrypted content."
            RemoteSignerMethodResponse.Error(
                id = this.requestId ?: this.nostrEvent.id,
                error = message,
                clientPubKey = this.nostrEvent.pubKey,
            )
        }

        else -> RemoteSignerMethodResponse.Error(
            id = nostrEvent.id,
            error = this.message ?: "An unknown error within remote signer occurred while parsing method.",
            clientPubKey = nostrEvent.pubKey,
        )
    }
}

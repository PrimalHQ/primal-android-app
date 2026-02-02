package net.primal.core.networking.nwc.nip47

/**
 * Exception thrown when an NWC (Nostr Wallet Connect) operation fails.
 * Contains the NIP-47 error code for proper error handling.
 */
class NwcException(
    val errorCode: String,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    companion object {
        fun fromNwcError(error: NwcError, cause: Throwable? = null): NwcException {
            return NwcException(
                errorCode = error.code,
                message = error.message,
                cause = cause,
            )
        }
    }
}

package net.primal.domain.nostr.cryptography

class SigningKeyNotFoundException(message: String? = null, cause: Throwable? = null) : SignatureException(
    message,
    cause,
)

package net.primal.android.core.errors

import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException

sealed class SignatureUiError {
    data object SigningRejected : SignatureUiError()
    data object SigningKeyNotFound : SignatureUiError()
}

fun SignatureException.asSignatureUiError() =
    when (this) {
        is SigningRejectedException -> SignatureUiError.SigningRejected
        is SigningKeyNotFoundException -> SignatureUiError.SigningKeyNotFound
        else -> throw NotImplementedError("Please provide a mapping for ${this.javaClass.simpleName}.")
    }

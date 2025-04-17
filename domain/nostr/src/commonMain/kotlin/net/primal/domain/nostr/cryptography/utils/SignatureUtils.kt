package net.primal.domain.nostr.cryptography.utils

import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.SignResult
import net.primal.domain.nostr.cryptography.SignatureException

fun SignResult.unwrapOrThrow(onFailure: ((SignatureException) -> Unit)? = null): NostrEvent =
    when (this) {
        is SignResult.Rejected -> {
            onFailure?.invoke(this.error)
            throw this.error
        }

        is SignResult.Signed -> {
            this.event
        }
    }

fun SignResult.getOrNull(onFailure: ((SignatureException) -> Unit)? = null): NostrEvent? =
    when (this) {
        is SignResult.Rejected -> {
            onFailure?.invoke(this.error)
            null
        }

        is SignResult.Signed -> {
            this.event
        }
    }

fun SignResult.getOrThrow(error: Throwable) =
    when (this) {
        is SignResult.Rejected -> throw error
        is SignResult.Signed -> this.event
    }

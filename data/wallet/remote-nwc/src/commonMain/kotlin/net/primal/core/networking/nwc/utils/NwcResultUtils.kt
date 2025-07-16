package net.primal.core.networking.nwc.utils

import net.primal.core.networking.nwc.NwcResult

fun <T> NwcResult<T>.unwrapOrThrow(onFailure: ((Exception) -> Unit)? = null): T =
    when (this) {
        is NwcResult.Failure -> {
            onFailure?.invoke(this.error)
            throw this.error
        }

        is NwcResult.Success -> {
            this.result
        }
    }

fun <T> NwcResult<T>.getOrNull(onFailure: ((Exception) -> Unit)? = null): T? =
    when (this) {
        is NwcResult.Failure -> {
            onFailure?.invoke(this.error)
            null
        }

        is NwcResult.Success -> {
            this.result
        }
    }

fun <T> NwcResult<T>.getOrThrow(error: Throwable) =
    when (this) {
        is NwcResult.Failure -> throw error
        is NwcResult.Success -> this.result
    }

fun <T> NwcResult<T>.getOrThrow() =
    when (this) {
        is NwcResult.Failure -> throw this.error
        is NwcResult.Success -> this.result
    }

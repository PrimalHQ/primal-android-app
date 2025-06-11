package net.primal.core.networking.nwc

sealed class NwcResult<T> {
    data class Success<T>(val result: T) : NwcResult<T>()
    data class Failure<T>(val error: Exception) : NwcResult<T>()
}

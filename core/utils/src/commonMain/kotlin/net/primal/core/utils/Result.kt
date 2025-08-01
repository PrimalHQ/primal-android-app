package net.primal.core.utils

import net.primal.core.utils.Result.Companion.failure
import net.primal.core.utils.Result.Companion.success
import net.primal.core.utils.Result.Failure
import net.primal.core.utils.Result.Success

@Suppress("UNCHECKED_CAST")
sealed class Result<T> {

    @PublishedApi
    internal data class Success<T>(val value: T) : Result<T>()

    @PublishedApi
    internal data class Failure<T>(val exception: Throwable) : Result<T>()

    /**
     * Returns `true` if this instance represents a successful outcome.
     * In this case [isFailure] returns `false`.
     */
    inline val isSuccess: Boolean get() = this is Success

    /**
     * Returns `true` if this instance represents a failed outcome.
     * In this case [isSuccess] returns `false`.
     */
    inline val isFailure: Boolean get() = this is Failure

    /**
     * Returns the encapsulated value if this instance represents [success][Result.isSuccess] or `null`
     * if it is [failure][Result.isFailure].
     *
     * This function is a shorthand for `getOrElse { null }` (see [getOrElse]) or
     * `fold(onSuccess = { it }, onFailure = { null })` (see [fold]).
     */
    fun getOrNull(): T? = fold(onSuccess = { it }, onFailure = { null })

    /**
     * Returns the encapsulated [Throwable] exception if this instance represents [failure][isFailure] or `null`
     * if it is [success][isSuccess].
     *
     * This function is a shorthand for `fold(onSuccess = { null }, onFailure = { it })` (see [fold]).
     */
    fun exceptionOrNull(): Throwable? = fold(onSuccess = { null }, onFailure = { it })

    /**
     * Companion object for [Result] class that contains its constructor functions
     * [success] and [failure].
     */
    companion object {
        /**
         * Returns an instance that encapsulates the given [value] as successful value.
         */
        fun <T> success(value: T): Result<T> = Success(value)

        /**
         * Returns an instance that encapsulates the given [Throwable] [exception] as failure.
         */
        fun <T> failure(exception: Throwable): Result<T> = Failure(exception = exception)
    }

    /**
     * Returns the encapsulated value if this instance represents [success][Result.isSuccess] or throws the encapsulated [Throwable] exception
     * if it is [failure][Result.isFailure].
     *
     * This function is a shorthand for `getOrElse { throw it }` (see [getOrElse]).
     */
    fun getOrThrow(): T =
        when (this) {
            is Failure<T> -> throw exception
            is Success<T> -> value
        }

    /**
     * Returns the encapsulated value if this instance represents [success][Result.isSuccess] or the
     * result of [onFailure] function for the encapsulated [Throwable] exception if it is [failure][Result.isFailure].
     *
     * Note, that this function rethrows any [Throwable] exception thrown by [onFailure] function.
     *
     * This function is a shorthand for `fold(onSuccess = { it }, onFailure = onFailure)` (see [fold]).
     */
    fun <R, T : R> getOrElse(onFailure: (Throwable) -> R): R = fold(onSuccess = { it as T }, onFailure = onFailure)

    /**
     * Returns the encapsulated value if this instance represents [success][Result.isSuccess] or the
     * [defaultValue] if it is [failure][Result.isFailure].
     *
     * This function is a shorthand for `getOrElse { defaultValue }` (see [getOrElse]).
     */
    fun <R, T : R> getOrDefault(defaultValue: R): R = fold(onSuccess = { it as T }, onFailure = { defaultValue })
}

/**
 * Calls the specified function [block] and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the [block] function execution and encapsulating it as a failure.
 */
inline fun <R> runCatching(block: () -> R): Result<R> {
    return try {
        success(block())
    } catch (e: Throwable) {
        failure(e)
    }
}

/**
 * Returns the result of [onSuccess] for the encapsulated value if this instance represents [success][Result.isSuccess]
 * or the result of [onFailure] function for the encapsulated [Throwable] exception if it is [failure][Result.isFailure].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [onSuccess] or by [onFailure] function.
 */
inline fun <R, T> Result<T>.fold(onSuccess: (value: T) -> R, onFailure: (error: Throwable) -> R): R =
    when (this) {
        is Failure<T> -> onFailure(exception)
        is Success<T> -> onSuccess(value)
    }

/**
 * Calls the specified function [block] with `this` value as its receiver and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the [block] function execution and encapsulating it as a failure.
 */
inline fun <T, R> T.runCatching(block: T.() -> R): Result<R> {
    return try {
        success(block())
    } catch (e: Throwable) {
        failure(e)
    }
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated value
 * if this instance represents [success][Result.isSuccess] or the
 * original encapsulated [Throwable] exception if it is [failure][Result.isFailure].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [transform] function.
 * See [mapCatching] for an alternative that encapsulates exceptions.
 */
inline fun <R, T> Result<T>.map(transform: (value: T) -> R): Result<R> =
    when (this) {
        is Success<T> -> success(transform(this.value))
        is Failure<T> -> failure(exception)
    }

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated value
 * if this instance represents [success][Result.isSuccess] or the
 * original encapsulated [Throwable] exception if it is [failure][Result.isFailure].
 *
 * This function catches any [Throwable] exception thrown by [transform] function and encapsulates it as a failure.
 * See [map] for an alternative that rethrows exceptions from `transform` function.
 */
inline fun <R, T> Result<T>.mapCatching(transform: (value: T) -> R): Result<R> =
    when (this) {
        is Success<T> -> runCatching { transform(value) }
        is Failure<T> -> failure(exception)
    }

/**
 * Performs the given [block] on the encapsulated value if this instance represents [success][Result.isSuccess].
 * Returns the original `Result` unchanged in case the [block] doesn't fail. If the [block] fails, its own
 * failure will be returned.
 *
 * In case of [Failure] block will be skipped and result bubbled allowing for handling of errors in one place.
 *
 * This function catches any [Throwable] exception thrown by [block] function and encapsulates it as a failure.
 * See [also] for an alternative that rethrows exceptions from [block] function.
 */
inline fun <T> Result<T>.alsoCatching(block: (value: T) -> Unit): Result<T> =
    when (this) {
        is Success<T> -> runCatching { block(value) }.map { this.value }
        is Failure<T> -> failure(exception)
    }

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated [Throwable] exception
 * if this instance represents [failure][Result.isFailure] or the
 * original encapsulated value if it is [success][Result.isSuccess].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [transform] function.
 * See [recoverCatching] for an alternative that encapsulates exceptions.
 */
inline fun <R, T : R> Result<T>.recover(transform: (exception: Throwable) -> R): Result<R> =
    when (this) {
        is Failure<T> -> success(transform(exception))
        is Success<T> -> success(value)
    }

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated [Throwable] exception
 * if this instance represents [failure][Result.isFailure] or the
 * original encapsulated value if it is [success][Result.isSuccess].
 *
 * This function catches any [Throwable] exception thrown by [transform] function and encapsulates it as a failure.
 * See [recover] for an alternative that rethrows exceptions.
 */
inline fun <R, T : R> Result<T>.recoverCatching(transform: (exception: Throwable) -> R): Result<R> =
    when (this) {
        is Failure<T> -> runCatching { transform(exception) }
        is Success<T> -> success(value)
    }

/**
 * Performs the given [action] on the encapsulated [Throwable] exception if this instance represents [failure][Result.isFailure].
 * Returns the original `Result` unchanged.
 */
inline fun <T> Result<T>.onFailure(action: (exception: Throwable) -> Unit): Result<T> {
    exceptionOrNull()?.let { action(it) }
    return this
}

/**
 * Performs the given [action] on the encapsulated value if this instance represents [success][Result.isSuccess].
 * Returns the original `Result` unchanged.
 */
inline fun <T> Result<T>.onSuccess(action: (value: T) -> Unit): Result<T> {
    getOrNull()?.let { action(it) }
    return this
}

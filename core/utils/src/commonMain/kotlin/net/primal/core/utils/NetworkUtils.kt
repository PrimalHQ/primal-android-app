package net.primal.core.utils

import io.github.aakira.napier.Napier

/**
 * Attempts to retrieve data using a `get` function. If the data is not present,
 * it triggers a `fetch` operation and retries the `get` call.
 *
 * This function is useful for lazy-loading data where a local cache or database
 * might return `null`, and a network or fallback fetch is required to populate it.
 *
 * @param fetch A suspending function that performs a fetch operation (e.g., network call, DB update).
 * @param get A suspending function that returns the desired value of type [T], or `null` if not available.
 * @param onFinally A callback that runs after the operation completes, regardless of success or failure.
 * @param onSuccess A callback that is invoked with the result if it is successfully obtained (non-null).
 *
 * @return The retrieved value of type [T], or `null` if an exception of type [E] occurred or the value is still `null` after fetch.
 *
 * @throws Exception If an exception occurs during the operation that is not of type [E], it is rethrown.
 */
suspend inline fun <T, reified E : Exception> fetchAndGet(
    noinline fetch: suspend () -> Unit,
    get: suspend () -> T?,
    onFinally: () -> Unit,
    onSuccess: (T) -> Unit,
): T? =
    try {
        (get() ?: fetch().run { get() })?.also { onSuccess(it) }
    } catch (error: Exception) {
        Napier.w(throwable = error) { error.message ?: "" }
        if (error is E) {
            null
        } else {
            throw error
        }
    } finally {
        onFinally()
    }

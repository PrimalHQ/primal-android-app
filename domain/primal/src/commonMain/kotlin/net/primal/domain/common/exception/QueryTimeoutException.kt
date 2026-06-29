package net.primal.domain.common.exception

import kotlin.time.Duration

/**
 * Thrown when a Primal caching query exceeds its time-to-first-byte / inter-emission
 * deadline (see `BasePrimalApiClient.collectQueryResult`).
 *
 * This is a real failure — NOT coroutine lifecycle cancellation — and subclasses
 * [NetworkException] so existing `catch (NetworkException)` handlers (RemoteMediators,
 * ViewModels) surface it as a visible, retryable error.
 *
 * @param verb The Primal verb that timed out, for diagnostics.
 * @param timeout The deadline that was exceeded.
 * @param cause The originating `TimeoutCancellationException`.
 */
class QueryTimeoutException(
    val verb: String? = null,
    val timeout: Duration,
    cause: Throwable? = null,
) : NetworkException(message = "Query timed out after $timeout [verb=$verb]", cause = cause)

package net.primal.domain.common.exception

/**
 * Thrown when a network-related failure occurs while executing a domain-level operation.
 *
 * This exception represents issues such as failed API calls, unreachable servers, timeouts,
 * or malformed responses from external systems.
 *
 * It is used in shared code and marked with `@Throws` to provide Swift-friendly error handling
 * in Kotlin Multiplatform projects.
 *
 * @param message Optional detail message describing the failure.
 * @param cause The underlying cause of the exception, if any.
 */
class NetworkException(message: String?, cause: Throwable?) : RuntimeException(message, cause)

package net.primal.core.networking.utils

import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import net.primal.domain.common.exception.NetworkException

private val RETRY_DELAY = 300.milliseconds.inWholeMilliseconds

@Suppress("TooGenericExceptionCaught")
suspend fun <T> retryNetworkCall(
    retries: Int = 1,
    delay: Long = RETRY_DELAY,
    retryOnException: KClass<out Exception> = NetworkException::class,
    onBeforeDelay: ((error: Exception) -> Unit)? = null,
    onBeforeTry: ((attempt: Int) -> Unit)? = null,
    block: suspend () -> T,
): T {
    repeat(retries) {
        try {
            onBeforeTry?.invoke(it)
            return block()
        } catch (error: Exception) {
            if (retryOnException.isInstance(error)) {
                onBeforeDelay?.invoke(error)
                delay(delay)
            } else {
                throw error
            }
        }
    }
    return block()
}

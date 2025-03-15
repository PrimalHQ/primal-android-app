package net.primal.core.networking.utils

import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import net.primal.core.networking.sockets.errors.WssException

private val RETRY_DELAY = 300.milliseconds.inWholeMilliseconds

suspend fun <T> retryNetworkCall(
    retries: Int = 1,
    delay: Long = RETRY_DELAY,
    onBeforeDelay: ((error: WssException) -> Unit)? = null,
    onBeforeTry: ((attempt: Int) -> Unit)? = null,
    block: suspend () -> T,
): T {
    repeat(retries) {
        try {
            onBeforeTry?.invoke(it)
            return block()
        } catch (error: WssException) {
            onBeforeDelay?.invoke(error)
            delay(delay)
        }
    }
    return block()
}

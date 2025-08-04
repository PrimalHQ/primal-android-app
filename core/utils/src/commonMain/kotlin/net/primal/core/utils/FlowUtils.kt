package net.primal.core.utils

import kotlin.time.Duration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

fun <T> Flow<T>.bufferCountOrTimeout(count: Int, timeout: Duration): Flow<List<T>> =
    channelFlow {
        val buffer = mutableListOf<T>()
        var timeoutJob: Job? = null

        suspend fun emitAndReset() {
            if (buffer.isNotEmpty()) {
                send(buffer.toList())
                buffer.clear()
            }
            timeoutJob?.cancel()
            timeoutJob = null
        }

        collect { value ->
            buffer.add(value)

            if (timeoutJob == null) {
                timeoutJob = launch {
                    delay(timeout)
                    emitAndReset()
                }
            }

            if (buffer.size >= count) {
                emitAndReset()
            }
        }

        emitAndReset()
    }

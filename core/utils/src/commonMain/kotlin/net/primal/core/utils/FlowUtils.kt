package net.primal.core.utils

import kotlin.time.Duration
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
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

/**
 * Collect upstream elements into a buffer, and whenever there has been
 * no new element for [inactivityTimeout], emit the entire buffer as a list
 * (and then clear it).
 */
@FlowPreview
fun <T> Flow<T>.batchOnInactivity(inactivityTimeout: Duration): Flow<List<T>> =
    flow {
        val buffer = mutableListOf<T>()
        this@batchOnInactivity
            .onEach { buffer += it }
            .debounce(inactivityTimeout)
            .collect {
                emit(buffer.toList())
                buffer.clear()
            }
    }

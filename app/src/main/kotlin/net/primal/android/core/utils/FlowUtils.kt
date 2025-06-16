package net.primal.android.core.utils

import kotlin.time.Duration
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

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

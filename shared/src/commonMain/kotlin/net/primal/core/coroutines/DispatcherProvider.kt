package net.primal.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher

internal interface DispatcherProvider {
    fun io(): CoroutineDispatcher
    fun main(): CoroutineDispatcher
}

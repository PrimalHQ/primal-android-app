package net.primal.core.utils.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class IOSDispatcherProvider : DispatcherProvider {
    override fun io(): CoroutineDispatcher = Dispatchers.Default
    override fun main(): CoroutineDispatcher = Dispatchers.Main
}

actual fun createDispatcherProvider(): DispatcherProvider = IOSDispatcherProvider()

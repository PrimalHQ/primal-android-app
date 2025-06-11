package net.primal.core.utils.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class JvmDispatcherProvider : DispatcherProvider {
    override fun io(): CoroutineDispatcher = Dispatchers.IO
    override fun main(): CoroutineDispatcher = Dispatchers.Main
}

actual fun createDispatcherProvider(): DispatcherProvider = JvmDispatcherProvider()

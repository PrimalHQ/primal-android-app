package net.primal.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class AndroidDispatcherProvider : DispatcherProvider {
    override fun io(): CoroutineDispatcher = Dispatchers.IO
    override fun main(): CoroutineDispatcher = Dispatchers.Main
}

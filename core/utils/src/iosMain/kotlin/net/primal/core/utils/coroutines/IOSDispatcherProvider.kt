package net.primal.core.utils.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.primal.core.utils.coroutines.DispatcherProvider

class IOSDispatcherProvider : DispatcherProvider {
    override fun io(): CoroutineDispatcher = Dispatchers.Default
    override fun main(): CoroutineDispatcher = Dispatchers.Main
}

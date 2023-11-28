package net.primal.android.core.coroutines

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Singleton
class CoroutineDispatcherProvider @Inject constructor() {

    fun io(): CoroutineDispatcher = Dispatchers.IO

    fun main(): CoroutineDispatcher = Dispatchers.Main

    fun default(): CoroutineDispatcher = Dispatchers.Default
}

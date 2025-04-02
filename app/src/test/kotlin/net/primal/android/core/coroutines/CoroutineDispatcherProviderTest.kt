package net.primal.android.core.coroutines

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.primal.core.utils.coroutines.DispatcherProvider
import org.junit.Test

class CoroutineDispatcherProviderTest {

    private class TestDispatcherProvider : DispatcherProvider {
        override fun io(): CoroutineDispatcher = Dispatchers.IO
        override fun main(): CoroutineDispatcher = Dispatchers.Main
    }

    private val dispatcherProvider = TestDispatcherProvider()

    @Test
    fun coroutineDispatcherProvider_returnsProperIoDispatcher() {
        dispatcherProvider.io() shouldBe Dispatchers.IO
    }

    @Test
    fun coroutineDispatcherProvider_returnsProperMainDispatcher() {
        dispatcherProvider.main() shouldBe Dispatchers.Main
    }
}

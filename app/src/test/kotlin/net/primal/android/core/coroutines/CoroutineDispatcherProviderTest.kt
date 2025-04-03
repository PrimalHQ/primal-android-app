package net.primal.android.core.coroutines

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import org.junit.Test

class CoroutineDispatcherProviderTest {

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

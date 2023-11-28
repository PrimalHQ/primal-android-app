package net.primal.android.core.coroutines

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import org.junit.Test

class CoroutineDispatcherProviderTest {

    @Test
    fun coroutineDispatcherProvider_returnsProperIoDispatcher() {
        CoroutineDispatcherProvider().io() shouldBe Dispatchers.IO
    }

    @Test
    fun coroutineDispatcherProvider_returnsProperMainDispatcher() {
        CoroutineDispatcherProvider().main() shouldBe Dispatchers.Main
    }

    @Test
    fun coroutineDispatcherProvider_returnsProperDefaultDispatcher() {
        CoroutineDispatcherProvider().default() shouldBe Dispatchers.Default
    }
}

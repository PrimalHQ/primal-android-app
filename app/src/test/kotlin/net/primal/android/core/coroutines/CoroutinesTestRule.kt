package net.primal.android.core.coroutines

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import net.primal.core.utils.coroutines.DispatcherProvider
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class CoroutinesTestRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {

    lateinit var dispatcherProvider: DispatcherProvider
        private set

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
        dispatcherProvider = mockk {
            every { io() } returns testDispatcher
            every { main() } returns testDispatcher
        }
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

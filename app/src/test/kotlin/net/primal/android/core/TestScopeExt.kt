package net.primal.android.core

import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.withContext

/**
 * Calls [advanceUntilIdle][TestCoroutineScheduler.advanceUntilIdle] and then switches the
 * context to [Dispatchers.Default][Dispatchers.Default] and delays the coroutine for
 * 42 milliseconds. It happens that sometimes (on Apple M1 machines)
 * [advanceUntilIdle()][TestCoroutineScheduler.advanceUntilIdle] doesn't advance enough
 * to idle which is causing tests to randomly fail (or randomly succeed).
 *
 * Use only if [advanceUntilIdle][TestCoroutineScheduler.advanceUntilIdle] fails.
 */
@ExperimentalCoroutinesApi
suspend fun TestScope.advanceUntilIdleAndDelay() {
    advanceUntilIdle()
    withContext(Dispatchers.Default) { delay(42.milliseconds) }
}

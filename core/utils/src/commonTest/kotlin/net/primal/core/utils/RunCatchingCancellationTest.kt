package net.primal.core.utils

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout

@OptIn(ExperimentalCoroutinesApi::class)
class RunCatchingCancellationTest {

    @Test
    fun `runCatching rethrows CancellationException instead of wrapping it as Failure`() {
        assertFailsWith<CancellationException> {
            runCatching<Unit> { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun `runCatching still wraps non-cancellation Throwables as Failure`() {
        val result = runCatching<Unit> { throw RuntimeException("boom") }
        assertTrue(result.isFailure)
        assertIs<RuntimeException>(result.exceptionOrNull())
        assertEquals("boom", result.exceptionOrNull()?.message)
    }

    @Test
    fun `runCatching returns Success when block completes normally`() {
        val result = runCatching { 42 }
        assertTrue(result.isSuccess)
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `receiver runCatching rethrows CancellationException`() {
        val receiver = "input"
        assertFailsWith<CancellationException> {
            receiver.runCatching<String, Unit> { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun `receiver runCatching still wraps non-cancellation Throwables as Failure`() {
        val receiver = "input"
        val result = receiver.runCatching<String, Unit> { throw RuntimeException("boom") }
        assertTrue(result.isFailure)
        assertIs<RuntimeException>(result.exceptionOrNull())
    }

    @Test
    fun `coroutine cancellation propagates through runCatching to parent scope`() =
        runTest {
            var bodyEntered = false
            var bodyCompleted = false
            val job: Job = launch {
                runCatching {
                    bodyEntered = true
                    awaitCancellation()
                }
                bodyCompleted = true
            }

            // Let the child coroutine reach awaitCancellation()
            testScheduler.runCurrent()
            assertTrue(bodyEntered, "child coroutine should have entered runCatching block")
            assertTrue(job.isActive, "child coroutine should still be suspended")

            job.cancelAndJoin()

            assertTrue(job.isCancelled, "job should be in cancelled state")
            assertTrue(!bodyCompleted, "code after runCatching must NOT run when scope is cancelled")
        }

    @Test
    fun `withTimeout firing inside runCatching propagates TimeoutCancellationException`() =
        runTest {
            var bodyCompleted = false
            assertFailsWith<TimeoutCancellationException> {
                withTimeout(100.milliseconds) {
                    runCatching { delay(1.seconds) }
                    bodyCompleted = true
                }
            }
            assertTrue(!bodyCompleted, "code after runCatching must NOT run when withTimeout fires")
        }

    @Test
    fun `runCatching rethrows kotlinx-coroutines CancellationException`() {
        assertFailsWith<CancellationException> {
            runCatching<Unit> { throw kotlinx.coroutines.CancellationException("cancelled") }
        }
    }

    @Test
    fun `receiver runCatching rethrows kotlinx-coroutines CancellationException`() {
        val receiver = "input"
        assertFailsWith<CancellationException> {
            receiver.runCatching<String, Unit> { throw kotlinx.coroutines.CancellationException("cancelled") }
        }
    }

    @Test
    fun `async with runCatching cancels Deferred and rethrows from await`() =
        runTest {
            val deferred: Deferred<Result<Nothing>> = async {
                runCatching { awaitCancellation() }
            }

            testScheduler.runCurrent()
            assertTrue(deferred.isActive, "deferred should be suspended at awaitCancellation()")

            deferred.cancel()

            assertFailsWith<CancellationException> { deferred.await() }
            assertTrue(deferred.isCancelled, "deferred must end in cancelled state, not completed-with-Failure")
        }

    @Test
    fun `receiver runCatching propagates parent cancellation to the scope`() =
        runTest {
            var bodyEntered = false
            var bodyCompleted = false
            val receiver = "input"
            val job: Job = launch {
                receiver.runCatching<String, Unit> {
                    bodyEntered = true
                    awaitCancellation()
                }
                bodyCompleted = true
            }

            testScheduler.runCurrent()
            assertTrue(bodyEntered, "child coroutine should have entered receiver runCatching block")
            assertTrue(job.isActive, "child coroutine should still be suspended")

            job.cancelAndJoin()

            assertTrue(job.isCancelled, "job should be in cancelled state")
            assertTrue(!bodyCompleted, "code after receiver runCatching must NOT run when scope is cancelled")
        }

    @Test
    fun `runCatching rethrows the exact CancellationException instance without wrapping`() {
        val sentinel = CancellationException("sentinel-marker")
        val caught = try {
            runCatching<Unit> { throw sentinel }
            null
        } catch (e: CancellationException) {
            e
        }
        assertSame(sentinel, caught, "rethrown exception must be the same instance, not a wrapper")
    }

    @Test
    fun `runCatching rethrows the exact kotlinx-coroutines CancellationException instance`() {
        val sentinel: CancellationException = kotlinx.coroutines.CancellationException("sentinel-marker")
        val caught = try {
            runCatching<Unit> { throw sentinel }
            null
        } catch (e: CancellationException) {
            e
        }
        assertSame(sentinel, caught, "rethrown exception must be the same instance, not a wrapper")
    }
}

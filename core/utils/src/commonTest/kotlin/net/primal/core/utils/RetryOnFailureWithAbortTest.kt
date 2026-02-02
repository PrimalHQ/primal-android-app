package net.primal.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import net.primal.core.utils.Result.Companion.failure
import net.primal.core.utils.Result.Companion.success

@OptIn(ExperimentalCoroutinesApi::class)
class RetryOnFailureWithAbortTest {

    @Test
    fun `success on first attempt returns immediately`() =
        runTest {
            var invocationCount = 0
            val block: suspend () -> Result<String> = {
                invocationCount++
                success("result")
            }

            val result = block.retryOnFailureWithAbort()

            assertTrue(result.isSuccess)
            assertEquals("result", result.getOrNull())
            assertEquals(1, invocationCount)
        }

    @Test
    fun `success after retries returns success`() =
        runTest {
            var invocationCount = 0
            val block: suspend () -> Result<String> = {
                invocationCount++
                if (invocationCount < 3) {
                    failure(RuntimeException("attempt $invocationCount"))
                } else {
                    success("result")
                }
            }

            val result = block.retryOnFailureWithAbort(times = 5, initialDelaySeconds = 1)

            assertTrue(result.isSuccess)
            assertEquals("result", result.getOrNull())
            assertEquals(3, invocationCount)
        }

    @Test
    fun `abort via shouldContinue returning false`() =
        runTest {
            var invocationCount = 0
            var shouldContinueCallCount = 0
            val block: suspend () -> Result<String> = {
                invocationCount++
                failure(RuntimeException("error"))
            }

            val result = block.retryOnFailureWithAbort(
                times = 5,
                initialDelaySeconds = 1,
                shouldContinue = {
                    shouldContinueCallCount++
                    shouldContinueCallCount <= 2
                },
            )

            assertTrue(result.isFailure)
            assertIs<IllegalStateException>(result.exceptionOrNull())
            assertEquals("Retry aborted by shouldContinue", result.exceptionOrNull()?.message)
            assertEquals(2, invocationCount)
            assertEquals(3, shouldContinueCallCount)
        }

    @Test
    fun `all retries exhausted calls onFinalFailure`() =
        runTest {
            var finalFailureCalled = false
            var finalError: Throwable? = null
            val testException = RuntimeException("persistent error")
            val block: suspend () -> Result<String> = {
                failure(testException)
            }

            val result = block.retryOnFailureWithAbort(
                times = 3,
                initialDelaySeconds = 1,
                onFinalFailure = { error ->
                    finalFailureCalled = true
                    finalError = error
                },
            )

            assertTrue(result.isFailure)
            assertTrue(finalFailureCalled)
            assertEquals(testException, finalError)
        }

    @Test
    fun `onRetry is called with correct parameters`() =
        runTest {
            val retryParams = mutableListOf<RetryParams>()
            var invocationCount = 0
            val block: suspend () -> Result<String> = {
                invocationCount++
                if (invocationCount < 4) {
                    failure(RuntimeException("error $invocationCount"))
                } else {
                    success("result")
                }
            }

            block.retryOnFailureWithAbort(
                times = 5,
                initialDelaySeconds = 2,
                onRetry = { attempt, remainingAttempts, delaySeconds, error ->
                    retryParams.add(RetryParams(attempt, remainingAttempts, delaySeconds, error.message ?: ""))
                },
            )

            assertEquals(3, retryParams.size)
            assertEquals(RetryParams(0, 4, 2, "error 1"), retryParams[0])
            assertEquals(RetryParams(1, 3, 4, "error 2"), retryParams[1])
            assertEquals(RetryParams(2, 2, 8, "error 3"), retryParams[2])
        }

    @Test
    fun `exponential backoff delays are correct`() =
        runTest {
            var invocationCount = 0
            val block: suspend () -> Result<String> = {
                invocationCount++
                if (invocationCount < 4) {
                    failure(RuntimeException("error"))
                } else {
                    success("result")
                }
            }

            val startTime = currentTime

            block.retryOnFailureWithAbort(
                times = 5,
                initialDelaySeconds = 1,
            )

            val elapsedTime = currentTime - startTime
            val expectedDelayMs = (1 + 2 + 4) * 1000L
            assertEquals(expectedDelayMs.milliseconds.inWholeMilliseconds, elapsedTime)
        }

    @Test
    fun `onFinalFailure not called on success`() =
        runTest {
            var finalFailureCalled = false
            val block: suspend () -> Result<String> = {
                success("result")
            }

            block.retryOnFailureWithAbort(
                onFinalFailure = { finalFailureCalled = true },
            )

            assertTrue(!finalFailureCalled)
        }

    @Test
    fun `onRetry not called on first success`() =
        runTest {
            var retryCalled = false
            val block: suspend () -> Result<String> = {
                success("result")
            }

            block.retryOnFailureWithAbort(
                onRetry = { _, _, _, _ -> retryCalled = true },
            )

            assertTrue(!retryCalled)
        }

    @Test
    fun `shouldContinue checked before first attempt`() =
        runTest {
            var invocationCount = 0
            val block: suspend () -> Result<String> = {
                invocationCount++
                success("result")
            }

            val result = block.retryOnFailureWithAbort(
                shouldContinue = { false },
            )

            assertTrue(result.isFailure)
            assertEquals(0, invocationCount)
        }

    private data class RetryParams(
        val attempt: Int,
        val remainingAttempts: Int,
        val delaySeconds: Int,
        val errorMessage: String,
    )
}

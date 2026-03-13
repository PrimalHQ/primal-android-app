package net.primal.networking.utils

import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import net.primal.core.networking.utils.retryNetworkCall
import net.primal.domain.common.exception.NetworkException

class RetryNetworkCallTest {

    @Test
    fun `retries on NetworkException by default and succeeds`() =
        runTest {
            var attempts = 0
            val result = retryNetworkCall(retries = 2, delay = 0) {
                attempts++
                if (attempts < 2) throw NetworkException("fail")
                "success"
            }
            assertEquals("success", result)
            assertEquals(2, attempts)
        }

    @Test
    fun `does not catch IOException by default`() =
        runTest {
            assertFailsWith<IOException> {
                retryNetworkCall(retries = 2, delay = 0) {
                    throw IOException("fail")
                }
            }
        }

    @Test
    fun `retries on custom exception class`() =
        runTest {
            var attempts = 0
            val result = retryNetworkCall(retries = 2, delay = 0, retryOnException = IOException::class) {
                attempts++
                if (attempts < 2) throw IOException("fail")
                "success"
            }
            assertEquals("success", result)
            assertEquals(2, attempts)
        }

    @Test
    fun `does not catch non-matching exception with custom class`() =
        runTest {
            assertFailsWith<NetworkException> {
                retryNetworkCall(retries = 2, delay = 0, retryOnException = IOException::class) {
                    throw NetworkException("fail")
                }
            }
        }

    @Test
    fun `makes final attempt after exhausting retries`() =
        runTest {
            var attempts = 0
            assertFailsWith<NetworkException> {
                retryNetworkCall(retries = 2, delay = 0) {
                    attempts++
                    throw NetworkException("fail")
                }
            }
            assertEquals(3, attempts) // 2 retries + 1 final attempt
        }
}

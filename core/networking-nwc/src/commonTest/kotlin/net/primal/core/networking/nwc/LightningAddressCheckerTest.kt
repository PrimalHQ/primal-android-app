package net.primal.core.networking.nwc

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import net.primal.core.utils.coroutines.DispatcherProvider

class LightningAddressCheckerTest {

    private fun buildMockHttpClient(responseStatus: HttpStatusCode): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when {
                        request.url.toString().contains("alex") -> respond(
                            content = "{}",
                            status = responseStatus,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )

                        else -> respondError(HttpStatusCode.NotFound)
                    }
                }
            }
        }
    }

    private val testDispatcherProvider = object : DispatcherProvider {
        override fun io(): CoroutineDispatcher = Dispatchers.Default
        override fun main(): CoroutineDispatcher = Dispatchers.Default
    }

    @Test
    fun validateLightningAddress_doesNotRaiseExceptionForValidAddress() =
        runTest {
            val checker = LightningAddressChecker(
                dispatcherProvider = testDispatcherProvider,
                httpClient = buildMockHttpClient(HttpStatusCode.OK),
            )

            checker.validateLightningAddress("alex@primal.net")
        }

    @Test
    fun validateLightningAddress_throwsExceptionForInvalidAddress() =
        runTest {
            val checker = LightningAddressChecker(
                dispatcherProvider = testDispatcherProvider,
                httpClient = buildMockHttpClient(HttpStatusCode.NotFound),
            )

            assertFailsWith<InvalidLud16Exception> {
                checker.validateLightningAddress("invalidaddress@domain.com")
            }
        }
}

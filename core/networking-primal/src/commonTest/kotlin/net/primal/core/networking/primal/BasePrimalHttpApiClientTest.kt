package net.primal.core.networking.primal

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import net.primal.core.config.AppConfigProvider
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.common.exception.QueryTimeoutException
import net.primal.domain.global.PrimalServerType

class BasePrimalHttpApiClientTest {

    private class FakeAppConfigProvider(private val url: String) : AppConfigProvider {
        var cacheUrlInvocations = 0
            private set

        override suspend fun cacheUrl(): StateFlow<String> {
            cacheUrlInvocations++
            return MutableStateFlow(url)
        }

        override suspend fun uploadUrl(): StateFlow<String> = MutableStateFlow(url)
        override suspend fun walletUrl(): StateFlow<String> = MutableStateFlow(url)
    }

    /** Mirrors ktor: the request job is cancelled with the timeout as the cancellation cause. */
    private class JobCancellation(override val cause: Throwable) : CancellationException("Parent job is Cancelling")

    private class FakePrimalHttpApi(
        private val error: Throwable? = null,
        private val response: JsonElement = JsonPrimitive("ok"),
        private val responseDelay: Duration = Duration.ZERO,
    ) : PrimalHttpApi {
        var lastUrl: String? = null
        var lastBody: JsonArray? = null

        override suspend fun request(url: String, body: JsonArray): JsonElement {
            lastUrl = url
            lastBody = body
            delay(responseDelay)
            error?.let { throw it }
            return response
        }
    }

    private fun clientOf(
        api: PrimalHttpApi,
        appConfigProvider: AppConfigProvider = FakeAppConfigProvider("wss://cache1.primal.net/v1"),
    ) = BasePrimalHttpApiClient(
        serverType = PrimalServerType.Caching,
        appConfigProvider = appConfigProvider,
        primalHttpApi = api,
    )

    @Test
    fun `query resolves the http api url and sends the positional request`() =
        runTest {
            val api = FakePrimalHttpApi()

            clientOf(api).query(PrimalCacheFilter(primalVerb = "import_events", optionsJson = """{"events":[]}"""))

            api.lastUrl shouldBe "https://cache1.primal.net/api"
            api.lastBody.toString() shouldBe """["import_events",{"events":[]}]"""
        }

    @Test
    fun `query resolves the api url flow once and reuses it for later queries`() =
        runTest {
            val appConfigProvider = FakeAppConfigProvider("wss://cache1.primal.net/v1")
            val client = clientOf(api = FakePrimalHttpApi(), appConfigProvider = appConfigProvider)

            repeat(times = 3) { client.query(PrimalCacheFilter(primalVerb = "import_events")) }

            appConfigProvider.cacheUrlInvocations shouldBe 1
        }

    @Test
    fun `query maps an exceeded deadline to QueryTimeoutException`() =
        runTest {
            val api = FakePrimalHttpApi(responseDelay = HTTP_API_QUERY_TIMEOUT * 2)

            val error = runCatching {
                clientOf(api).query(PrimalCacheFilter(primalVerb = "import_events"))
            }.exceptionOrNull()

            val timeout = error.shouldBeInstanceOf<QueryTimeoutException>()
            timeout.verb shouldBe "import_events"
            timeout.timeout shouldBe HTTP_API_QUERY_TIMEOUT
        }

    @Test
    fun `query maps a request timeout to QueryTimeoutException`() =
        runTest {
            val api = FakePrimalHttpApi(
                error = HttpRequestTimeoutException(url = "https://cache1.primal.net/api", timeoutMillis = 10_000),
            )

            val error = runCatching {
                clientOf(api).query(PrimalCacheFilter(primalVerb = "import_events"))
            }.exceptionOrNull()

            val timeout = error.shouldBeInstanceOf<QueryTimeoutException>()
            timeout.verb shouldBe "import_events"
            timeout.timeout shouldBe HTTP_API_QUERY_TIMEOUT
        }

    @Test
    fun `query maps a cancelled request job to QueryTimeoutException`() =
        runTest {
            val api = FakePrimalHttpApi(
                error = JobCancellation(
                    cause = HttpRequestTimeoutException(
                        url = "https://cache1.primal.net/api",
                        timeoutMillis = 10_000,
                    ),
                ),
            )

            val error = runCatching {
                clientOf(api).query(PrimalCacheFilter(primalVerb = "import_events"))
            }.exceptionOrNull()

            error.shouldBeInstanceOf<QueryTimeoutException>()
        }

    @Test
    fun `query propagates cancellation that is not a timeout`() =
        runTest {
            val api = FakePrimalHttpApi(error = CancellationException("caller went away"))

            val error = runCatching {
                clientOf(api).query(PrimalCacheFilter(primalVerb = "import_events"))
            }.exceptionOrNull()

            error.shouldBeInstanceOf<CancellationException>()
        }

    @Test
    fun `query maps other failures to a plain NetworkException`() =
        runTest {
            val api = FakePrimalHttpApi(error = IllegalStateException("boom"))

            val error = runCatching {
                clientOf(api).query(PrimalCacheFilter(primalVerb = "import_events"))
            }.exceptionOrNull()

            error.shouldBeInstanceOf<NetworkException>()
            (error is QueryTimeoutException) shouldBe false
        }
}

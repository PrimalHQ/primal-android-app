package net.primal.android.notes.translation

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.coroutines.runBlocking
import org.junit.Test

class LibreTranslateProviderTest {

    private val settings = TranslationSettings(
        enabled = true,
        endpoint = "https://translate.example",
        apiKey = "secret-key",
        targetLanguage = "en",
    )

    private fun providerResponding(
        status: HttpStatusCode,
        body: String,
        onRequest: (HttpRequestData) -> Unit = {},
    ): LibreTranslateProvider {
        val engine = MockEngine { request ->
            onRequest(request)
            respond(
                content = ByteReadChannel(body),
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        return LibreTranslateProvider(settings, HttpClient(engine))
    }

    @Test
    fun `translate parses translatedText and string detectedLanguage`() = runBlocking {
        var sentBody = ""
        val provider = providerResponding(
            status = HttpStatusCode.OK,
            body = """{"translatedText":"Hello world","detectedLanguage":"es"}""",
            onRequest = { sentBody = (it.body as io.ktor.http.content.TextContent).text },
        )
        val result = provider.translate(text = "Hola mundo", sourceLanguage = "auto", targetLanguage = "en")
        result.translatedText shouldBe "Hello world"
        result.detectedLanguage shouldBe "es"
        sentBody shouldContain "\"q\":\"Hola mundo\""
        sentBody shouldContain "\"api_key\":\"secret-key\""
    }

    @Test
    fun `translate parses object detectedLanguage and rejects numeric`() = runBlocking {
        val objectForm = providerResponding(
            status = HttpStatusCode.OK,
            body = """{"translatedText":"Bonjour","detectedLanguage":{"language":"fr","confidence":92}}""",
        )
        objectForm.translate(text = "Hello", sourceLanguage = "auto", targetLanguage = "fr")
            .detectedLanguage shouldBe "fr"

        val numericForm = providerResponding(
            status = HttpStatusCode.OK,
            body = """{"translatedText":"Bonjour","detectedLanguage":42}""",
        )
        numericForm.translate(text = "Hello", sourceLanguage = "auto", targetLanguage = "fr")
            .detectedLanguage shouldBe null
    }

    @Test
    fun `translate omits api_key when no key is configured`() = runBlocking {
        var sentBody = ""
        val engine = MockEngine { request ->
            sentBody = (request.body as io.ktor.http.content.TextContent).text
            respond(
                content = ByteReadChannel("""{"translatedText":"Hello"}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val provider = LibreTranslateProvider(settings.copy(apiKey = ""), HttpClient(engine))
        provider.translate(text = "Hola", sourceLanguage = "auto", targetLanguage = "en")
        sentBody shouldNotContain "api_key"
    }

    @Test
    fun `translate maps http errors and empty results to ProviderError`() = runBlocking {
        val failing = LibreTranslateProvider(
            settings,
            HttpClient(MockEngine { respondError(HttpStatusCode.InternalServerError) }),
        )
        val thrown = runCatching {
            failing.translate(text = "Hola", sourceLanguage = "auto", targetLanguage = "en")
        }.exceptionOrNull()
        (thrown as? TranslationException)?.code shouldBe TranslationErrorCode.ProviderError

        val empty = providerResponding(HttpStatusCode.OK, """{"translatedText":""}""")
        val thrownEmpty = runCatching {
            empty.translate(text = "Hola", sourceLanguage = "auto", targetLanguage = "en")
        }.exceptionOrNull()
        (thrownEmpty as? TranslationException)?.code shouldBe TranslationErrorCode.ProviderError
    }

    @Test
    fun `listLanguages parses entries and skips malformed ones`() = runBlocking {
        val provider = providerResponding(
            status = HttpStatusCode.OK,
            body = """[{"code":"en","name":"English"},{"code":"es","name":"Spanish"},{"name":"NoCode"}]""",
        )
        val languages = provider.listLanguages()
        languages.map { it.code } shouldBe listOf("en", "es")
        languages[1].name shouldBe "Spanish"
    }
}

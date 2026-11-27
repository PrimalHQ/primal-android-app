package net.primal.android.notes.translation

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TranslationServiceTest {

    private class FakeProvider(
        private val responder: (text: String) -> ProviderTranslation,
    ) : TranslationProvider {
        val receivedTexts = mutableListOf<String>()
        val receivedTargets = mutableListOf<String>()
        var translateCalls = 0

        override suspend fun translate(
            text: String,
            sourceLanguage: String,
            targetLanguage: String,
        ): ProviderTranslation {
            translateCalls++
            receivedTexts.add(text)
            receivedTargets.add(targetLanguage)
            return responder(text)
        }

        override suspend fun listLanguages(): List<SupportedLanguage> {
            return listOf(SupportedLanguage(code = "en", name = "English"))
        }
    }

    private fun service(
        settings: TranslationSettings,
        provider: FakeProvider,
        cache: TranslationCache = TranslationCache(),
    ): TranslationService {
        return TranslationService(
            settingsProvider = { settings },
            providerFactory = { provider },
            cache = cache,
        )
    }

    private val enabledSettings = TranslationSettings(
        enabled = true,
        endpoint = "https://libretranslate.example",
        apiKey = "",
        targetLanguage = "en",
    )

    @Test
    fun `provider never receives protected tokens and result restores them`() = runBlocking {
        val npub = "npub1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq90f3g"
        val provider = FakeProvider { text ->
            ProviderTranslation(translatedText = text.replace("Hola", "Hello"), detectedLanguage = "es")
        }
        val result = service(enabledSettings, provider)
            .translateNote("Hola a todos, sigan a nostr:$npub para mas noticias")
        provider.receivedTexts.single() shouldNotContain npub
        result.translatedText shouldBe "Hello a todos, sigan a nostr:$npub para mas noticias"
        result.detectedLanguage shouldBe "es"
        result.fromCache shouldBe false
    }

    @Test
    fun `second translation of the same note is served from cache`() = runBlocking {
        val provider = FakeProvider { text ->
            ProviderTranslation(translatedText = "$text [translated]", detectedLanguage = null)
        }
        val service = service(enabledSettings, provider)
        val content = "Este es un mensaje de prueba para todos los amigos"
        service.translateNote(content)
        val second = service.translateNote(content)
        provider.translateCalls shouldBe 1
        second.fromCache shouldBe true
    }

    @Test
    fun `disabled feature throws Disabled`() = runBlocking {
        val provider = FakeProvider { ProviderTranslation("x", null) }
        val thrown = runCatching {
            service(enabledSettings.copy(enabled = false), provider)
                .translateNote("Este es un mensaje de prueba para todos")
        }.exceptionOrNull()
        (thrown as? TranslationException)?.code shouldBe TranslationErrorCode.Disabled
        provider.translateCalls shouldBe 0
    }

    @Test
    fun `missing endpoint throws NotConfigured`() = runBlocking {
        val provider = FakeProvider { ProviderTranslation("x", null) }
        val thrown = runCatching {
            service(enabledSettings.copy(endpoint = ""), provider)
                .translateNote("Este es un mensaje de prueba para todos")
        }.exceptionOrNull()
        (thrown as? TranslationException)?.code shouldBe TranslationErrorCode.NotConfigured
    }

    @Test
    fun `api key over plain http to a remote host throws NotConfigured`() = runBlocking {
        val provider = FakeProvider { ProviderTranslation("x", null) }
        val thrown = runCatching {
            service(
                enabledSettings.copy(endpoint = "http://libretranslate.example", apiKey = "secret-key"),
                provider,
            ).translateNote("Este es un mensaje de prueba para todos")
        }.exceptionOrNull()
        (thrown as? TranslationException)?.code shouldBe TranslationErrorCode.NotConfigured
        provider.translateCalls shouldBe 0
    }

    @Test
    fun `api key over localhost http is allowed`() = runBlocking {
        val provider = FakeProvider { text -> ProviderTranslation("$text ok", null) }
        val result = service(
            enabledSettings.copy(endpoint = "http://localhost:5000", apiKey = "secret-key"),
            provider,
        ).translateNote("Este es un mensaje de prueba para todos")
        result.translatedText shouldBe "Este es un mensaje de prueba para todos ok"
    }

    @Test
    fun `full language tags are sent to the provider verbatim`() = runBlocking {
        val provider = FakeProvider { ProviderTranslation(it, "es") }
        service(enabledSettings.copy(targetLanguage = "zh-Hans"), provider)
            .translateNote("Este es un mensaje de prueba para todos")
        provider.receivedTargets.single() shouldBe "zh-Hans"
    }

    @Test
    fun `endpoint normalization strips query and fragment but keeps subpaths`() {
        TranslationEndpoints.normalizeEndpoint("https://example.com/lt?key=1#frag") shouldBe "https://example.com/lt"
        TranslationEndpoints.normalizeEndpoint("https://example.com/lt/") shouldBe "https://example.com/lt"
        TranslationEndpoints.normalizeEndpoint("https://example.com/translate") shouldBe "https://example.com"
    }

    @Test
    fun `endpoint validation requires a parseable http url with a host and no userinfo`() {
        TranslationEndpoints.isValidEndpoint("https://translate.example") shouldBe true
        TranslationEndpoints.isValidEndpoint("http://192.168.1.10:5000") shouldBe true
        TranslationEndpoints.isValidEndpoint("https://user@translate.example") shouldBe false
        TranslationEndpoints.isValidEndpoint("https://") shouldBe false
        TranslationEndpoints.isValidEndpoint("not a url") shouldBe false
        TranslationEndpoints.isValidEndpoint("ftp://translate.example") shouldBe false
    }

    @Test
    fun `endpoint security check cannot be bypassed with userinfo`() {
        TranslationEndpoints.isSecureForApiKey("http://localhost:5000@attacker.example") shouldBe false
        TranslationEndpoints.isSecureForApiKey("http://user@localhost:5000") shouldBe false
        TranslationEndpoints.isSecureForApiKey("https://localhost@attacker.example") shouldBe false
        TranslationEndpoints.isSecureForApiKey("http://localhost:5000") shouldBe true
        TranslationEndpoints.isSecureForApiKey("https://anything.example") shouldBe true
    }

    @Test
    fun `provider failure is wrapped as Unreachable`() = runBlocking {
        val provider = FakeProvider { throw IllegalStateException("boom") }
        val thrown = runCatching {
            service(enabledSettings, provider)
                .translateNote("Este es un mensaje de prueba para todos")
        }.exceptionOrNull()
        (thrown as? TranslationException)?.code shouldBe TranslationErrorCode.Unreachable
    }
}

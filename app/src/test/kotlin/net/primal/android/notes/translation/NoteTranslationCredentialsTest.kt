package net.primal.android.notes.translation

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.user.domain.NoteTranslationProvider
import net.primal.android.user.domain.NoteTranslationSettings
import org.junit.Test

class NoteTranslationCredentialsTest {

    @Test
    fun `saved credentials include an explicit provider and endpoint binding`() {
        val settings = NoteTranslationSettings(
            provider = NoteTranslationProvider.DeepL,
            libreTranslateUrl = "https://lt.example",
            apiKey = "deepl-only",
        )
        val saved = Json.encodeToString(NoteTranslationSettings.serializer(), settings)
        val fields = Json.parseToJsonElement(saved).jsonObject
        fields["apiKeyProvider"]?.jsonPrimitive?.content shouldBe "DeepL"
        fields["apiKeyLibreTranslateUrl"]?.jsonPrimitive?.content shouldBe "https://lt.example"
    }

    @Test
    fun `serialization cannot reauthorize a key for a different provider`() {
        val original = NoteTranslationSettings(provider = NoteTranslationProvider.DeepL, apiKey = "deepl-only")
        val changed = original.copy(provider = NoteTranslationProvider.Google)
        val saved = Json.encodeToString(NoteTranslationSettings.serializer(), changed)
        val loaded = Json.decodeFromString<NoteTranslationSettings>(saved)
        NoteTranslationConfig.resolveNetworkRoute(loaded) shouldBe null
    }

    @Test
    fun `serialization cannot reauthorize a key for a different libretranslate endpoint`() {
        val original = NoteTranslationSettings(
            provider = NoteTranslationProvider.LibreTranslate,
            libreTranslateUrl = "https://first.test",
            apiKey = "first-only",
        )
        val changed = original.copy(libreTranslateUrl = "https://second.test")
        val saved = Json.encodeToString(NoteTranslationSettings.serializer(), changed)
        val loaded = Json.decodeFromString<NoteTranslationSettings>(saved)
        val route = NoteTranslationConfig.resolveNetworkRoute(loaded) as NetworkTranslationRoute.LibreTranslate
        route.apiKey shouldBe null
    }

    @Test
    fun `legacy settings retain their selected provider without authorizing later switches`() {
        val loaded = Json.decodeFromString<NoteTranslationSettings>("""{"provider":"Google","apiKey":"google-only"}""")
        loaded.apiKeyForProvider() shouldBe "google-only"
        loaded.copy(provider = NoteTranslationProvider.DeepL).apiKeyForProvider() shouldBe null
    }
}

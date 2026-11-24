package net.primal.android.notes.translation

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import net.primal.android.user.domain.ContentDisplaySettings
import net.primal.android.user.domain.NoteTranslationProvider
import org.junit.Test

class NoteTranslationSettingsSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `legacy content display json gets fail-closed translation defaults`() {
        val decoded = json.decodeFromString<ContentDisplaySettings>("""{"autoPlayVideos":1}""")
        decoded.noteTranslation.enabled shouldBe true
        decoded.noteTranslation.libreTranslateUrl shouldBe ""
        decoded.noteTranslation.apiKey shouldBe ""
        decoded.noteTranslation.provider shouldBe NoteTranslationProvider.PreferOnDevice
        NoteTranslationConfig.resolveNetworkRoute(decoded.noteTranslation) shouldBe null
    }

    @Test
    fun `round-trip persists user-owned provider fields`() {
        val settings = ContentDisplaySettings(
            noteTranslation = net.primal.android.user.domain.NoteTranslationSettings(
                enabled = true,
                targetLanguageCode = "es",
                provider = NoteTranslationProvider.LibreTranslate,
                libreTranslateUrl = "https://lt.home",
                apiKey = "secret",
            ),
        )
        val encoded = json.encodeToString(ContentDisplaySettings.serializer(), settings)
        val decoded = json.decodeFromString(ContentDisplaySettings.serializer(), encoded)
        decoded.noteTranslation.targetLanguageCode shouldBe "es"
        decoded.noteTranslation.libreTranslateUrl shouldBe "https://lt.home"
        decoded.noteTranslation.apiKey shouldBe "secret"
        decoded.noteTranslation.provider shouldBe NoteTranslationProvider.LibreTranslate
    }
}

package net.primal.android.notes.translation

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.util.Locale
import org.junit.Test

class NoteTranslateTest {

    @Test
    fun `normalizeLangCode uses primary subtag`() {
        NoteTranslate.normalizeLangCode("en-US") shouldBe "en"
        NoteTranslate.normalizeLangCode("it_IT") shouldBe "it"
        NoteTranslate.normalizeLangCode("pt-BR") shouldBe "pt"
    }

    @Test
    fun `normalizeLangCode handles chinese variants`() {
        NoteTranslate.normalizeLangCode("zh") shouldBe "zh-CN"
        NoteTranslate.normalizeLangCode("zh-CN") shouldBe "zh-CN"
        NoteTranslate.normalizeLangCode("zh-TW") shouldBe "zh-TW"
        NoteTranslate.normalizeLangCode("zh-HK") shouldBe "zh-TW"
    }

    @Test
    fun `resolveTargetLanguage prefers settings locale`() {
        NoteTranslate.resolveTargetLanguage(
            settingsLocale = "fr-FR",
            deviceLocale = Locale.US,
        ) shouldBe "fr"
    }

    @Test
    fun `resolveTargetLanguage falls back to device locale`() {
        NoteTranslate.resolveTargetLanguage(
            settingsLocale = null,
            deviceLocale = Locale.GERMANY,
        ) shouldBe "de"
    }

    @Test
    fun `parseGoogleResponse nested array`() {
        val (text, source) = NoteTranslate.parseGoogleResponse("""[["Ciao amici","en"]]""")
        text shouldBe "Ciao amici"
        source shouldBe "en"
    }

    @Test
    fun `parseGoogleResponse string array`() {
        val (text, source) = NoteTranslate.parseGoogleResponse("""["Ciao"," amici"]""")
        text shouldBe "Ciao amici"
        source shouldBe null
    }

    @Test
    fun `chunkText splits near whitespace`() {
        val chunks = NoteTranslate.chunkText("hello world friends", size = 12)
        chunks.joinToString(" ") shouldBe "hello world friends"
        (chunks.size >= 2) shouldBe true
    }

    @Test
    fun `buildGoogleTranslateWebUrl includes params`() {
        val url = NoteTranslate.buildGoogleTranslateWebUrl("Hello", "it")
        url shouldContain "translate.google.com"
        url shouldContain "tl=it"
        url shouldContain "sl=auto"
    }
}

package net.primal.android.notes.translation

import io.kotest.matchers.shouldBe
import java.util.Locale
import org.junit.Test

class NoteTranslationLanguagesTest {

    @Test
    fun `device language preserves region and script`() {
        val original = Locale.getDefault()
        try {
            listOf("zh-TW", "zh-Hant", "pt-PT", "en-GB").forEach { tag ->
                Locale.setDefault(Locale.forLanguageTag(tag))
                NoteTranslationLanguages.deviceLanguageCode() shouldBe tag
            }
        } finally {
            Locale.setDefault(original)
        }
    }
}

package net.primal.android.notes.feed.note.translation

import io.kotest.matchers.shouldBe
import net.primal.android.R
import org.junit.Test

class NoteTranslationControlsTest {

    @Test
    fun `already translated notes explain that translation is unnecessary`() {
        NoteTranslationException.AlreadyInTargetLanguage.messageResource() shouldBe
            R.string.note_translation_already_in_target
    }

    @Test
    fun `language detection failures use a specific message`() {
        NoteTranslationException.LanguageNotDetected.messageResource() shouldBe
            R.string.note_translation_language_not_detected
    }

    @Test
    fun `missing system service uses the unavailable message`() {
        NoteTranslationException.Unavailable.messageResource() shouldBe
            R.string.note_translation_unavailable
    }

    @Test
    fun `translation errors remain retryable`() {
        NoteTranslationException.Failed.messageResource() shouldBe R.string.note_translation_failed
    }
}

package net.primal.android.notes.feed.note.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class NoteTranslationTest {

    @Test
    @Config(sdk = [31])
    fun `translateNote uses Android translation service when available`() {
        val context = RecordingContext()

        context.translateNote(noteContent = "Hola 🌎", targetLanguage = "en")

        context.startedIntents.single().apply {
            action shouldBe Intent.ACTION_TRANSLATE
            getCharSequenceExtra(Intent.EXTRA_TEXT) shouldBe "Hola 🌎"
        }
    }

    @Test
    @Config(sdk = [31])
    fun `translateNote falls back to browser when translation service is unavailable`() {
        val context = RecordingContext(failTranslationIntent = true)

        context.translateNote(noteContent = "Hola & adiós", targetLanguage = "en")

        context.startedIntents.last().apply {
            action shouldBe Intent.ACTION_VIEW
            data?.getQueryParameter("sl") shouldBe "auto"
            data?.getQueryParameter("tl") shouldBe "en"
            data?.getQueryParameter("text") shouldBe "Hola & adiós"
        }
    }

    @Test
    fun `buildGoogleTranslateUrl encodes note and target language`() {
        buildGoogleTranslateUrl(
            noteContent = "Hola, WNBA! #baloncesto",
            targetLanguage = "en",
        ) shouldBe "https://translate.google.com/?sl=auto&tl=en&text=Hola%2C+WNBA%21+%23baloncesto&op=translate"
    }

    private class RecordingContext(
        private val failTranslationIntent: Boolean = false,
    ) : ContextWrapper(ApplicationProvider.getApplicationContext<Context>()) {
        val startedIntents = mutableListOf<Intent>()

        override fun startActivity(intent: Intent) {
            startedIntents += intent
            if (failTranslationIntent && intent.action == Intent.ACTION_TRANSLATE) {
                throw ActivityNotFoundException()
            }
        }
    }
}

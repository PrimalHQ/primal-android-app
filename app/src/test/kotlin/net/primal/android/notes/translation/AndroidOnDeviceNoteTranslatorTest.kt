package net.primal.android.notes.translation

import android.content.Context
import android.view.translation.TranslationManager
import android.view.translation.Translator
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.Executor
import java.util.function.Consumer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import net.primal.android.notes.translation.device.AndroidOnDeviceNoteTranslator
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], manifest = Config.NONE)
class AndroidOnDeviceNoteTranslatorTest {

    @Test
    fun `cancelling translator creation propagates cancellation`() =
        runTest {
            val fixture = fixture()
            var completed = false
            val job = launch {
                fixture.client.translate("Hello friends", "es")
                completed = true
            }
            runCurrent()
            job.cancel()
            runCurrent()
            completed shouldBe false
        }

    @Test
    fun `late callback destroys the unused translator`() =
        runTest {
            val fixture = fixture()
            val translator = mockk<Translator>(relaxed = true)
            val job = launch { fixture.client.translate("Hello friends", "es") }
            runCurrent()
            job.cancel()
            runCurrent()
            fixture.callback.accept(translator)
            verify(exactly = 1) { translator.destroy() }
        }

    @Test
    fun `cancellation after callback destroys the undelivered translator`() =
        runTest {
            val fixture = fixture()
            val translator = mockk<Translator>(relaxed = true)
            val job = launch { fixture.client.translate("Hello friends", "es") }
            runCurrent()
            fixture.callback.accept(translator)
            job.cancel()
            runCurrent()
            verify(exactly = 1) { translator.destroy() }
        }

    private fun fixture(): Fixture {
        val fixture = Fixture()
        val manager = mockk<TranslationManager> {
            every { createOnDeviceTranslator(any(), any(), any()) } answers {
                fixture.callback = thirdArg()
            }
        }
        val context = mockk<Context> {
            every { getSystemService(TranslationManager::class.java) } returns manager
            every { mainExecutor } returns Executor { it.run() }
        }
        fixture.client = AndroidOnDeviceNoteTranslator(context)
        return fixture
    }

    private class Fixture {
        lateinit var callback: Consumer<Translator?>
        lateinit var client: AndroidOnDeviceNoteTranslator
    }
}

package net.primal.android.notes.translation

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import net.primal.android.notes.translation.device.OnDeviceNoteTranslator
import net.primal.android.notes.translation.remote.RemoteNoteTranslator
import net.primal.android.user.domain.NoteTranslationProvider
import net.primal.android.user.domain.NoteTranslationSettings
import net.primal.core.utils.coroutines.DispatcherProvider
import org.junit.Test

class NoteTranslationEngineTest {

    private val dispatcher = StandardTestDispatcher()
    private val dispatcherProvider = object : DispatcherProvider {
        override fun io(): CoroutineDispatcher = dispatcher
        override fun main(): CoroutineDispatcher = dispatcher
    }

    @Test
    fun `fail-closed when libretranslate is selected without a url`() =
        runTest(dispatcher) {
            val engine = engine()
            val outcome = engine.translate(
                text = "Hola amigos del nostr",
                settings = NoteTranslationSettings(provider = NoteTranslationProvider.LibreTranslate),
            )
            outcome shouldBe NoteTranslationOutcome.NotConfigured
        }

    @Test
    fun `fail-closed when on-device misses and no fallback is configured`() =
        runTest(dispatcher) {
            val engine = engine(onDevice = OnDeviceNoteTranslator { _, _ -> null })
            val outcome = engine.translate(
                text = "Hola amigos del nostr",
                settings = NoteTranslationSettings(),
            )
            outcome shouldBe NoteTranslationOutcome.NotConfigured
        }

    @Test
    fun `on-device success never calls the network client`() =
        runTest(dispatcher) {
            var remoteCalls = 0
            val engine = engine(
                onDevice = OnDeviceNoteTranslator { text, _ -> "Hello $text" },
                remote = RemoteNoteTranslator { _, _, _ ->
                    remoteCalls += 1
                    error("network should not run")
                },
            )
            val outcome = engine.translate(
                text = "Hola amigos del nostr",
                settings = NoteTranslationSettings(),
            )
            outcome.shouldBeInstanceOf<NoteTranslationOutcome.Translated>()
            (outcome as NoteTranslationOutcome.Translated).source shouldBe NoteTranslationSource.OnDevice
            remoteCalls shouldBe 0
        }

    @Test
    fun `network fallback restores protected tokens`() =
        runTest(dispatcher) {
            val invoice = "lnbc888550n1pnp6fz9pp5als09l5nfj9pkqk7mpj6cz6075nd4v95ljz0p65n8zkz03p75t3sdp"
            val engine = engine(
                onDevice = OnDeviceNoteTranslator { _, _ -> null },
                remote = RemoteNoteTranslator { _, text, _ ->
                    text.replace("Hola", "Hello")
                },
            )
            val outcome = engine.translate(
                text = "Hola $invoice amigos",
                settings = NoteTranslationSettings(libreTranslateUrl = "https://lt.home"),
            )
            outcome.shouldBeInstanceOf<NoteTranslationOutcome.Translated>()
            val translated = outcome as NoteTranslationOutcome.Translated
            translated.source shouldBe NoteTranslationSource.LibreTranslate
            translated.text shouldBe "Hello $invoice amigos"
        }

    @Test
    fun `already in target is an info outcome not a failure`() =
        runTest(dispatcher) {
            val engine = engine(
                remote = RemoteNoteTranslator { _, text, _ -> text },
            )
            val settings = NoteTranslationSettings(
                provider = NoteTranslationProvider.LibreTranslate,
                libreTranslateUrl = "https://lt.home",
            )
            val outcome = engine.translate(text = "Hello friends on nostr", settings = settings)
            outcome.shouldBeInstanceOf<NoteTranslationOutcome.AlreadyInTarget>()
        }

    @Test
    fun `successful translations are cached`() =
        runTest(dispatcher) {
            var calls = 0
            val engine = engine(
                remote = RemoteNoteTranslator { _, _, _ ->
                    calls += 1
                    "Hallo Freunde"
                },
            )
            val settings = NoteTranslationSettings(
                provider = NoteTranslationProvider.DeepL,
                apiKey = "abc:fx",
            )
            engine.translate(text = "Hello friends on nostr", settings = settings)
            val second = engine.translate(text = "Hello friends on nostr", settings = settings)
            calls shouldBe 1
            second.shouldBeInstanceOf<NoteTranslationOutcome.Translated>()
        }

    @Test
    fun `google provider failures surface as Failed`() =
        runTest(dispatcher) {
            val engine = engine(
                remote = RemoteNoteTranslator { _, _, _ -> error("quota") },
            )
            val outcome = engine.translate(
                text = "Hola amigos del nostr",
                settings = NoteTranslationSettings(
                    provider = NoteTranslationProvider.Google,
                    apiKey = "gcp",
                ),
            )
            outcome.shouldBeInstanceOf<NoteTranslationOutcome.Failed>()
        }

    private fun engine(
        onDevice: OnDeviceNoteTranslator = OnDeviceNoteTranslator { _, _ -> null },
        remote: RemoteNoteTranslator = RemoteNoteTranslator { _, _, _ -> error("unused") },
    ) = NoteTranslationEngine(
        remoteClient = remote,
        onDeviceTranslator = onDevice,
        dispatcherProvider = dispatcherProvider,
    )
}

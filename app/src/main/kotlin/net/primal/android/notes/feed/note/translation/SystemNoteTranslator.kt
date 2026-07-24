package net.primal.android.notes.feed.note.translation

import android.content.Context
import android.icu.util.ULocale
import android.os.Build
import android.os.CancellationSignal
import android.view.textclassifier.TextClassificationManager
import android.view.textclassifier.TextLanguage
import android.view.translation.TranslationContext
import android.view.translation.TranslationManager
import android.view.translation.TranslationRequest
import android.view.translation.TranslationRequestValue
import android.view.translation.TranslationResponse
import android.view.translation.TranslationResponseValue
import android.view.translation.TranslationSpec
import android.view.translation.Translator
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal data class TranslatedNote(
    val text: String,
    val targetLanguage: String,
)

internal sealed class NoteTranslationException : Exception() {
    data object Unavailable : NoteTranslationException()
    data object LanguageNotDetected : NoteTranslationException()
    data object AlreadyInTargetLanguage : NoteTranslationException()
    data object Failed : NoteTranslationException()
}

@RequiresApi(Build.VERSION_CODES.S)
internal class SystemNoteTranslator(
    private val context: Context,
) {
    suspend fun translate(text: String): TranslatedNote {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            throw NoteTranslationException.Unavailable
        }

        val sourceLocale = detectLanguage(text)
        val targetLocale = ULocale.forLocale(context.resources.configuration.locales[0])
        if (sourceLocale.language == targetLocale.language) {
            throw NoteTranslationException.AlreadyInTargetLanguage
        }

        val translatedText = translate(text = text, sourceLocale = sourceLocale, targetLocale = targetLocale)
        return TranslatedNote(
            text = translatedText,
            targetLanguage = targetLocale.getDisplayLanguage(targetLocale),
        )
    }

    private suspend fun detectLanguage(text: String): ULocale = withContext(Dispatchers.Default) {
        val classifier = context.getSystemService(TextClassificationManager::class.java)?.textClassifier
            ?: throw NoteTranslationException.Unavailable
        val result = classifier.detectLanguage(TextLanguage.Request.Builder(text).build())
        if (result.localeHypothesisCount == 0) {
            throw NoteTranslationException.LanguageNotDetected
        }

        val locale = result.getLocale(0)
        if (result.getConfidenceScore(locale) < MIN_LANGUAGE_CONFIDENCE) {
            throw NoteTranslationException.LanguageNotDetected
        }
        locale
    }

    private suspend fun translate(
        text: String,
        sourceLocale: ULocale,
        targetLocale: ULocale,
    ): String = suspendCancellableCoroutine { continuation ->
        val manager = context.getSystemService(TranslationManager::class.java)
        if (manager == null) {
            continuation.resumeWithException(NoteTranslationException.Unavailable)
            return@suspendCancellableCoroutine
        }

        val translationContext = TranslationContext.Builder(
            TranslationSpec(sourceLocale, TranslationSpec.DATA_FORMAT_TEXT),
            TranslationSpec(targetLocale, TranslationSpec.DATA_FORMAT_TEXT),
        ).build()

        manager.createOnDeviceTranslator(translationContext, context.mainExecutor) { translator ->
            if (translator == null) {
                continuation.resumeWithException(NoteTranslationException.Unavailable)
                return@createOnDeviceTranslator
            }
            if (!continuation.isActive) {
                translator.destroy()
                return@createOnDeviceTranslator
            }

            requestTranslation(
                translator = translator,
                text = text,
                continuation = continuation,
            )
        }
    }

    private fun requestTranslation(
        translator: Translator,
        text: String,
        continuation: CancellableContinuation<String>,
    ) {
        val cancellationSignal = CancellationSignal()
        val request = TranslationRequest.Builder()
            .setTranslationRequestValues(listOf(TranslationRequestValue.forText(text)))
            .build()

        continuation.invokeOnCancellation {
            cancellationSignal.cancel()
            translator.destroy()
        }

        translator.translate(request, cancellationSignal, context.mainExecutor) { response ->
            val result = response.translationResponseValues[0]
            val translatedText = result?.text?.toString()
            val isSuccessful = response.translationStatus == TranslationResponse.TRANSLATION_STATUS_SUCCESS &&
                result?.statusCode == TranslationResponseValue.STATUS_SUCCESS &&
                !translatedText.isNullOrBlank()

            translator.destroy()
            if (isSuccessful) {
                continuation.resume(translatedText)
            } else {
                continuation.resumeWithException(NoteTranslationException.Failed)
            }
        }
    }

    private companion object {
        const val MIN_LANGUAGE_CONFIDENCE = 0.5f
    }
}

package net.primal.android.core.translation

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stores user's translation preferences locally using SharedPreferences.
 */
@Singleton
class TranslationPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    private val _targetLanguage = MutableStateFlow(
        prefs.getString(KEY_TARGET_LANGUAGE, null)
            ?: TranslationService.getDefaultLanguageCode(),
    )
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()

    fun setTargetLanguage(languageCode: String) {
        prefs.edit().putString(KEY_TARGET_LANGUAGE, languageCode).apply()
        _targetLanguage.value = languageCode
    }

    fun getTargetLanguageName(): String {
        val code = _targetLanguage.value
        return TranslationService.supportedLanguages.find { it.code == code }?.name ?: "English"
    }

    companion object {
        private const val PREFS_NAME = "translation_preferences"
        private const val KEY_TARGET_LANGUAGE = "target_language"
    }
}

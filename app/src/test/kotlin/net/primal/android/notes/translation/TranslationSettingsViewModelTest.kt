package net.primal.android.notes.translation

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.primal.android.settings.translation.TranslationSettingsContract.UiEvent
import net.primal.android.settings.translation.TranslationSettingsViewModel
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.ContentDisplaySettings
import net.primal.android.user.domain.NoteTranslationProvider
import net.primal.android.user.domain.NoteTranslationSettings
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.repository.UserRepository
import net.primal.core.testing.CoroutinesTestRule
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TranslationSettingsViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    @Test
    fun `changing provider never forwards the previous provider key`() =
        runTest {
            val viewModel =
                viewModel(NoteTranslationSettings(provider = NoteTranslationProvider.DeepL, apiKey = "deepl-key"))
            advanceUntilIdle()
            viewModel.setEvent(UiEvent.UpdateProvider(NoteTranslationProvider.Google))
            advanceUntilIdle()
            viewModel.uiState.value.apiKey shouldBe ""
            viewModel.uiState.value.provider shouldBe NoteTranslationProvider.Google
        }

    @Test
    fun `changing libretranslate endpoint clears its key`() =
        runTest {
            val viewModel = viewModel(
                NoteTranslationSettings(libreTranslateUrl = "https://first.test", apiKey = "first-key"),
            )
            advanceUntilIdle()
            viewModel.setEvent(UiEvent.UpdateLibreTranslateUrl("https://second.test"))
            advanceUntilIdle()
            viewModel.uiState.value.apiKey shouldBe ""
        }

    @Test
    fun `selecting the same provider preserves its key`() =
        runTest {
            val viewModel =
                viewModel(NoteTranslationSettings(provider = NoteTranslationProvider.DeepL, apiKey = "deepl-key"))
            advanceUntilIdle()
            viewModel.setEvent(UiEvent.UpdateProvider(NoteTranslationProvider.DeepL))
            advanceUntilIdle()
            viewModel.uiState.value.apiKey shouldBe "deepl-key"
        }

    @Test
    fun `queued provider change and key entry bind the new key to its selected provider`() =
        runTest {
            val viewModel =
                viewModel(NoteTranslationSettings(provider = NoteTranslationProvider.DeepL, apiKey = "deepl-key"))
            advanceUntilIdle()
            viewModel.setEvent(UiEvent.UpdateProvider(NoteTranslationProvider.Google))
            viewModel.setEvent(UiEvent.UpdateApiKey("google-key"))
            advanceUntilIdle()
            viewModel.uiState.value.provider shouldBe NoteTranslationProvider.Google
            viewModel.uiState.value.apiKey shouldBe "google-key"
            viewModel.uiState.value.ready shouldBe true
        }

    private fun viewModel(settings: NoteTranslationSettings): TranslationSettingsViewModel {
        val accounts = MutableStateFlow(
            UserAccount.EMPTY.copy(contentDisplaySettings = ContentDisplaySettings(noteTranslation = settings)),
        )
        val store = mockk<ActiveAccountStore> {
            every { activeUserAccount } returns accounts
            every { activeUserId() } returns "test-user"
        }
        val repository = mockk<UserRepository> {
            coEvery { updateContentDisplaySettings(any(), any()) } coAnswers {
                val reducer = secondArg<ContentDisplaySettings.() -> ContentDisplaySettings>()
                accounts.value = accounts.value.copy(
                    contentDisplaySettings = accounts.value.contentDisplaySettings.reducer(),
                )
            }
        }
        return TranslationSettingsViewModel(repository, store)
    }
}

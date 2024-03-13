package net.primal.android.auth.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.auth.onboarding.OnboardingContract.UiEvent
import net.primal.android.auth.onboarding.OnboardingContract.UiState
import net.primal.android.auth.onboarding.api.OnboardingApi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.profile.domain.ProfileMetadata
import timber.log.Timber

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val onboardingApi: OnboardingApi,
    private val createAccountHandler: CreateAccountHandler,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        fetchInterests()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.InterestSelected -> setState {
                        copy(suggestions = suggestions.toMutableList().apply { add(it.suggestion) })
                    }
                    is UiEvent.InterestUnselected -> setState {
                        copy(suggestions = suggestions.toMutableList().apply { remove(it.suggestion) })
                    }
                    is UiEvent.ProfileAboutYouUpdated -> setState {
                        copy(profileAboutYou = it.aboutYou)
                    }
                    is UiEvent.ProfileAvatarUriChanged -> setState {
                        copy(avatarUri = it.avatarUri)
                    }
                    is UiEvent.ProfileBannerUriChanged -> setState {
                        copy(bannerUri = it.bannerUri)
                    }
                    is UiEvent.ProfileDisplayNameUpdated -> setState {
                        copy(profileDisplayName = it.displayName)
                    }
                    UiEvent.RequestNextStep -> setState {
                        copy(currentStep = this.currentStep.nextStep())
                    }
                    UiEvent.RequestPreviousStep -> setState {
                        copy(currentStep = this.currentStep.previousStep())
                    }
                    UiEvent.CreateNostrProfile -> {
                        createNostrAccount()
                    }
                    UiEvent.DismissError -> setState {
                        copy(error = null)
                    }
                }
            }
        }

    private fun fetchInterests() =
        viewModelScope.launch {
            try {
                setState { copy(working = true) }
                val response = retry(times = 3) {
                    withContext(dispatcherProvider.io()) {
                        onboardingApi.getFollowSuggestions()
                    }
                }
                setState { copy(allSuggestions = response.suggestions) }
            } catch (error: IOException) {
                Timber.e(error)
            } finally {
                setState { copy(working = false) }
            }
        }

    private suspend fun <T> retry(times: Int, block: suspend (Int) -> T): T {
        repeat(times) {
            try {
                return block(it)
            } catch (error: IOException) {
                Timber.w(error)
                delay(DELAY * (it + 1))
            }
        }
        return block(times)
    }

    private fun createNostrAccount() =
        viewModelScope.launch {
            try {
                setState { copy(working = true) }
                val uiState = state.value
                val userId = createAccountHandler.createNostrAccount(
                    profileMetadata = uiState.asProfileMetadata(),
                    interests = uiState.suggestions,
                )
                setState { copy(userId = userId) }
            } catch (error: UnsuccessfulFileUpload) {
                Timber.w(error)
                setState { copy(error = UiState.OnboardingError.ImageUploadFailed(error)) }
            } catch (error: CreateAccountHandler.AccountCreationException) {
                Timber.w(error)
                setState { copy(error = UiState.OnboardingError.CreateAccountFailed(error)) }
            } finally {
                setState { copy(working = false) }
            }
        }

    private fun OnboardingStep.nextStep() = OnboardingStep.fromIndex(this.index + 1)

    private fun OnboardingStep.previousStep() = OnboardingStep.fromIndex(this.index - 1)

    private fun UiState.asProfileMetadata(): ProfileMetadata =
        ProfileMetadata(
            username = null,
            displayName = this.profileDisplayName,
            about = this.profileAboutYou,
            localPictureUri = this.avatarUri,
            localBannerUri = this.bannerUri,
            remoteBannerUrl = DEFAULT_BANNER_URL,
        )

    companion object {
        private const val DELAY = 300L
        private const val DEFAULT_BANNER_URL = "https://m.primal.net/HQTd.jpg"
    }
}

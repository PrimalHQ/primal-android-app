package net.primal.android.auth.onboarding.account

import android.net.Uri
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
import net.primal.android.auth.onboarding.account.OnboardingContract.UiEvent
import net.primal.android.auth.onboarding.account.OnboardingContract.UiState
import net.primal.android.auth.onboarding.account.api.OnboardingApi
import net.primal.android.auth.repository.CreateAccountHandler
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.crypto.CryptoUtils
import net.primal.android.networking.primal.upload.PrimalFileUploader
import net.primal.android.networking.primal.upload.UnsuccessfulFileUpload
import net.primal.android.networking.primal.upload.domain.UploadJob
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.domain.ProfileMetadata
import timber.log.Timber

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val onboardingApi: OnboardingApi,
    private val createAccountHandler: CreateAccountHandler,
    private val fileUploader: PrimalFileUploader,
) : ViewModel() {

    private val keyPair = CryptoUtils.generateHexEncodedKeypair()

    private var avatarUploadJob: UploadJob? = null
    private var bannerUploadJob: UploadJob? = null

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

                    is UiEvent.ProfileAvatarUriChanged -> updateAvatarPhoto(it.avatarUri)
                    is UiEvent.ProfileBannerUriChanged -> updateBannerPhoto(it.bannerUri)

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

                    UiEvent.DismissError -> setState { copy(error = null) }

                    is UiEvent.SetFollowsCustomizing -> setState { copy(customizeSuggestions = it.customizing) }
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
                avatarUploadJob?.job?.join()
                bannerUploadJob?.job?.join()
                val uiState = state.value
                withContext(dispatcherProvider.io()) {
                    createAccountHandler.createNostrAccount(
                        privateKey = keyPair.privateKey,
                        profileMetadata = uiState.asProfileMetadata(),
                        interests = uiState.suggestions,
                    )
                }
                setState { copy(accountCreated = true) }
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

    private fun updateAvatarPhoto(avatarUri: Uri?) {
        setState { copy(avatarUri = avatarUri) }
        avatarUploadJob.cancel()
        avatarUploadJob = null
        if (avatarUri != null) {
            val uploadId = PrimalFileUploader.generateRandomUploadId()
            val job = viewModelScope.launch {
                try {
                    val uploadResult = withContext(dispatcherProvider.io()) {
                        fileUploader.uploadFile(keyPair = keyPair, uri = avatarUri)
                    }
                    setState { copy(avatarRemoteUrl = uploadResult.remoteUrl) }
                } catch (error: UnsuccessfulFileUpload) {
                    Timber.w(error)
                } catch (error: WssException) {
                    Timber.w(error)
                }
            }
            avatarUploadJob = UploadJob(job = job, id = uploadId)
        }
    }

    private fun updateBannerPhoto(bannerUri: Uri?) {
        setState { copy(bannerUri = bannerUri) }
        bannerUploadJob.cancel()
        bannerUploadJob = null
        if (bannerUri != null) {
            val uploadId = PrimalFileUploader.generateRandomUploadId()
            val job = viewModelScope.launch {
                try {
                    val uploadResult = withContext(dispatcherProvider.io()) {
                        fileUploader.uploadFile(keyPair = keyPair, uri = bannerUri)
                    }
                    setState { copy(bannerRemoteUrl = uploadResult.remoteUrl) }
                } catch (error: UnsuccessfulFileUpload) {
                    Timber.w(error)
                } catch (error: WssException) {
                    Timber.w(error)
                }
            }
            bannerUploadJob = UploadJob(job = job, id = uploadId)
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
            remotePictureUrl = this.avatarRemoteUrl,
            localBannerUri = this.bannerUri,
            remoteBannerUrl = this.bannerRemoteUrl ?: DEFAULT_BANNER_URL,
        )

    private fun UploadJob?.cancel() {
        if (this == null) return

        viewModelScope.launch {
            this@cancel.job.cancel()
            runCatching {
                fileUploader.cancelUpload(keyPair = keyPair, uploadId = this@cancel.id)
            }
        }
    }

    companion object {
        private const val DELAY = 300L
        private const val DEFAULT_BANNER_URL = "https://m.primal.net/HQTd.jpg"
    }
}

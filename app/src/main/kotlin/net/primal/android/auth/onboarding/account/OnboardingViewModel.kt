package net.primal.android.auth.onboarding.account

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.auth.onboarding.account.OnboardingContract.UiEvent
import net.primal.android.auth.onboarding.account.OnboardingContract.UiState
import net.primal.android.auth.repository.CreateAccountHandler
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.core.networking.blossom.AndroidPrimalBlossomUploadService
import net.primal.core.networking.blossom.BlossomException
import net.primal.core.networking.blossom.UploadJob
import net.primal.core.networking.blossom.UploadResult
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.signOrThrow
import net.primal.domain.nostr.cryptography.utils.CryptoUtils
import net.primal.domain.nostr.cryptography.utils.hexToNsecHrp

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val createAccountHandler: CreateAccountHandler,
    private val primalUploadService: AndroidPrimalBlossomUploadService,
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
        fetchFollowPacks()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.ProfileAboutYouUpdated -> setState { copy(profileAboutYou = event.aboutYou) }
                    is UiEvent.ProfileAvatarUriChanged -> updateAvatarPhoto(event.avatarUri)
                    is UiEvent.ProfileBannerUriChanged -> updateBannerPhoto(event.bannerUri)
                    is UiEvent.ProfileDisplayNameUpdated -> setState { copy(profileDisplayName = event.displayName) }
                    UiEvent.RequestNextStep -> setState { copy(currentStep = this.currentStep.nextStep()) }
                    UiEvent.RequestPreviousStep -> setState { copy(currentStep = this.currentStep.previousStep()) }
                    UiEvent.CreateNostrProfile -> createNostrAccount()
                    UiEvent.DismissError -> setState { copy(error = null) }
                    is UiEvent.TogglePackExpanded -> togglePackExpanded(event)
                    is UiEvent.ToggleFollowUser -> toggleFollowUser(event)
                    is UiEvent.ToggleFollowAllInPack -> toggleFollowAllInPack(event)
                }
            }
        }

    private fun fetchFollowPacks() =
        viewModelScope.launch {
            setState { copy(working = true) }
            runCatching { onboardingRepository.fetchFollowPacks() }
                .onSuccess { followPacks ->
                    setState { copy(followPacks = followPacks) }
                }
                .onFailure { error ->
                    Napier.e(throwable = error) { "Failed to fetch follow packs." }
                }
            setState { copy(working = false) }
        }

    private fun createNostrAccount() =
        viewModelScope.launch {
            try {
                setState { copy(working = true) }
                avatarUploadJob?.job?.join()
                bannerUploadJob?.job?.join()
                val uiState = state.value
                createAccountHandler.createNostrAccount(
                    privateKey = keyPair.privateKey,
                    profileMetadata = uiState.asProfileMetadata(),
                    followedUserIds = uiState.followedUserIds,
                )
                setState { copy(accountCreated = true, accountCreationStep = AccountCreationStep.AccountCreated) }
            } catch (error: BlossomException) {
                Napier.w(throwable = error) { "Failed to create Nostr account due to BlossomException." }
                setState { copy(error = UiState.OnboardingError.ImageUploadFailed(error)) }
            } catch (error: CreateAccountHandler.AccountCreationException) {
                Napier.w(throwable = error) { "Failed to create Nostr account." }
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
            val job = viewModelScope.launch {
                try {
                    val uploadResult = primalUploadService.upload(
                        uri = avatarUri,
                        userId = keyPair.pubKey,
                        onSignRequested = { it.signOrThrow(nsec = keyPair.privateKey.hexToNsecHrp()) },
                    )
                    if (uploadResult is UploadResult.Success) {
                        setState { copy(avatarRemoteUrl = uploadResult.remoteUrl) }
                    }
                } catch (error: NetworkException) {
                    Napier.w(throwable = error) { "Failed to upload avatar photo due to network error." }
                } catch (error: SignatureException) {
                    Napier.w(throwable = error) { "Failed to upload avatar photo due to signature error." }
                }
            }
            avatarUploadJob = UploadJob(job = job)
        }
    }

    private fun updateBannerPhoto(bannerUri: Uri?) {
        setState { copy(bannerUri = bannerUri) }
        bannerUploadJob.cancel()
        bannerUploadJob = null
        if (bannerUri != null) {
            val job = viewModelScope.launch {
                try {
                    val uploadResult = primalUploadService.upload(
                        uri = bannerUri,
                        userId = keyPair.pubKey,
                        onSignRequested = {
                            it.signOrThrow(keyPair.privateKey.hexToNsecHrp())
                        },
                    )
                    if (uploadResult is UploadResult.Success) {
                        setState { copy(bannerRemoteUrl = uploadResult.remoteUrl) }
                    }
                } catch (error: NetworkException) {
                    Napier.w(throwable = error) { "Failed to upload banner photo due to network error." }
                } catch (error: SignatureException) {
                    Napier.w(throwable = error) { "Failed to upload banner photo due to signature error." }
                }
            }
            bannerUploadJob = UploadJob(job = job)
        }
    }

    private fun OnboardingStep.nextStep(): OnboardingStep {
        return OnboardingStep.entries.find { it.index == this.index + 1 } ?: this
    }

    private fun OnboardingStep.previousStep(): OnboardingStep {
        return OnboardingStep.entries.find { it.index == this.index - 1 } ?: this
    }

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

    private fun UploadJob?.cancel() = this?.job?.cancel()

    private fun togglePackExpanded(event: UiEvent.TogglePackExpanded) {
        setState {
            val newExpanded = if (event.packName in expandedPackNames) {
                expandedPackNames - event.packName
            } else {
                expandedPackNames + event.packName
            }
            copy(expandedPackNames = newExpanded)
        }
    }

    private fun toggleFollowUser(event: UiEvent.ToggleFollowUser) {
        setState {
            val newFollowed = if (event.userId in followedUserIds) {
                followedUserIds - event.userId
            } else {
                followedUserIds + event.userId
            }
            copy(followedUserIds = newFollowed)
        }
    }

    private fun toggleFollowAllInPack(event: UiEvent.ToggleFollowAllInPack) {
        setState {
            val pack = followPacks.find { it.name == event.packName } ?: return@setState this
            val allMemberIds = pack.members.map { it.userId }.toSet()
            val allFollowed = allMemberIds.all { it in followedUserIds }
            val newFollowed = if (allFollowed) {
                followedUserIds - allMemberIds
            } else {
                followedUserIds + allMemberIds
            }
            copy(followedUserIds = newFollowed)
        }
    }

    companion object {
        internal const val DEFAULT_BANNER_URL =
            "https://blossom.primal.net/c15e22a2a8d1c7971f86adc758f944f3cbec6ef791fafd2604d85ee6beadaabb.png"
    }
}

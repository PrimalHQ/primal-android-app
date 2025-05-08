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
import net.primal.android.auth.onboarding.account.ui.model.FollowGroup
import net.primal.android.auth.onboarding.account.ui.model.FollowGroupMember
import net.primal.android.auth.repository.CreateAccountHandler
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.core.networking.blossom.AndroidPrimalBlossomUploadService
import net.primal.core.networking.blossom.BlossomException
import net.primal.core.networking.blossom.UploadJob
import net.primal.core.networking.blossom.UploadResult
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.ContentMetadata
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.signOrThrow
import net.primal.domain.nostr.cryptography.utils.CryptoUtils
import net.primal.domain.nostr.cryptography.utils.hexToNsecHrp
import timber.log.Timber

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val onboardingApi: OnboardingApi,
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
        fetchInterests()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.InterestSelected -> addFollowGroupByInterest(event)

                    is UiEvent.InterestUnselected -> removeFollowGroupByInterest(event)

                    is UiEvent.ProfileAboutYouUpdated -> setState { copy(profileAboutYou = event.aboutYou) }

                    is UiEvent.ProfileAvatarUriChanged -> updateAvatarPhoto(event.avatarUri)

                    is UiEvent.ProfileBannerUriChanged -> updateBannerPhoto(event.bannerUri)

                    is UiEvent.ProfileDisplayNameUpdated -> setState { copy(profileDisplayName = event.displayName) }

                    UiEvent.RequestNextStep -> setState { copy(currentStep = this.currentStep.nextStep()) }

                    UiEvent.RequestPreviousStep -> setState { copy(currentStep = this.currentStep.previousStep()) }

                    UiEvent.CreateNostrProfile -> createNostrAccount()

                    UiEvent.DismissError -> setState { copy(error = null) }

                    is UiEvent.SetFollowsCustomizing -> setState { copy(customizeFollows = event.customizing) }

                    is UiEvent.ToggleFollowEvent -> toggleFollowMemberFollowing(event)

                    is UiEvent.ToggleGroupFollowEvent -> toggleFollowGroupFollowing(event)

                    UiEvent.KeepRecommendedFollows -> keepRecommendedFollows()

                    UiEvent.AcknowledgeNostrKeyCreation -> setState {
                        copy(
                            accountCreationStep = AccountCreationStep.ZapsIntroduction,
                        )
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

                val allGroupSuggestions = response.suggestions.map {
                    FollowGroup(
                        name = it.group,
                        members = it.members.map { member ->
                            FollowGroupMember(
                                name = member.name,
                                userId = member.userId,
                                metadata = response.metadata[member.userId]?.content
                                    .decodeFromJsonStringOrNull<ContentMetadata>(),
                            )
                        },
                    )
                }
                setState { copy(allSuggestions = allGroupSuggestions) }
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
                createAccountHandler.createNostrAccount(
                    privateKey = keyPair.privateKey,
                    profileMetadata = uiState.asProfileMetadata(),
                    interests = uiState.selectedSuggestions,
                )
                setState { copy(accountCreated = true, accountCreationStep = AccountCreationStep.AccountCreated) }
            } catch (error: BlossomException) {
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
                    Timber.w(error)
                } catch (error: SignatureException) {
                    Timber.w(error)
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
                    Timber.w(error)
                } catch (error: SignatureException) {
                    Timber.w(error)
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

    private fun toggleFollowMemberFollowing(event: UiEvent.ToggleFollowEvent) {
        setState {
            copy(
                selectedSuggestions = this.selectedSuggestions.toMutableList().apply {
                    val groupIndex = indexOfFirst { it.name == event.groupName }
                    val group = if (groupIndex != -1) this[groupIndex] else null

                    val memberIndex = group?.members?.indexOfFirst { it.userId == event.userId } ?: -1
                    val member = if (memberIndex != -1) group?.members?.get(memberIndex) else null

                    val updatedMember = member?.copy(followed = !member.followed)

                    if (updatedMember != null) {
                        this[groupIndex] = this[groupIndex].copy(
                            members = this[groupIndex].members.toMutableList().apply {
                                this[memberIndex] = updatedMember
                            },
                        )
                    }
                },
            )
        }
    }

    private fun toggleFollowGroupFollowing(event: UiEvent.ToggleGroupFollowEvent) {
        val groupIndex = _state.value.selectedSuggestions.indexOfFirst { it.name == event.groupName }
        val group = if (groupIndex != -1) _state.value.selectedSuggestions[groupIndex] else null

        if (group != null) {
            val isFollowingAll = group.members.all { it.followed }
            setState {
                copy(
                    selectedSuggestions = this.selectedSuggestions.toMutableList().apply {
                        this[groupIndex] = this[groupIndex].copy(
                            members = this[groupIndex].members.map {
                                it.copy(followed = !isFollowingAll)
                            },
                        )
                    },
                )
            }
        }
    }

    private fun removeFollowGroupByInterest(event: UiEvent.InterestUnselected) {
        setState {
            copy(
                selectedSuggestions = selectedSuggestions.toMutableList().apply {
                    removeIf { it.name == event.groupName }
                },
            )
        }
    }

    private fun addFollowGroupByInterest(event: UiEvent.InterestSelected) {
        val followGroup = _state.value.allSuggestions.find { it.name == event.groupName }
        if (followGroup != null) {
            setState {
                copy(
                    selectedSuggestions = selectedSuggestions.toMutableList().apply {
                        add(followGroup)
                    },
                )
            }
        }
    }

    private fun keepRecommendedFollows() {
        setState {
            copy(
                selectedSuggestions = this.selectedSuggestions.toMutableList().apply {
                    forEachIndexed { index, followGroup ->
                        this[index] = followGroup.copy(
                            members = followGroup.members.map {
                                it.copy(followed = true)
                            },
                        )
                    }
                },
            )
        }
    }

    companion object {
        private const val DELAY = 300L
        private const val DEFAULT_BANNER_URL = "https://m.primal.net/HQTd.jpg"
    }
}

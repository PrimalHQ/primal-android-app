package net.primal.android.auth.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.auth.AuthRepository
import net.primal.android.auth.create.CreateAccountContract.SideEffect
import net.primal.android.auth.create.CreateAccountContract.UiEvent
import net.primal.android.auth.create.CreateAccountContract.UiState
import net.primal.android.auth.create.api.RecommendedFollowsApi
import net.primal.android.auth.create.ui.RecommendedFollow
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.BOOTSTRAP_RELAYS
import net.primal.android.user.repository.UserRepository

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val recommendedFollowsApi: RecommendedFollowsApi,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) =
        viewModelScope.launch {
            _effect.send(effect)
        }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.GoToProfilePreviewStepEvent -> setState {
                        copy(
                            currentStep = UiState.CreateAccountStep.PROFILE_PREVIEW,
                        )
                    }
                    is UiEvent.GoToNostrCreatedStepEvent -> createNostrAccount()
                    is UiEvent.GoToFollowContactsStepEvent -> fetchRecommendedFollows()
                    is UiEvent.GoBack -> goBack()
                    is UiEvent.FinishEvent -> finish()
                    is UiEvent.AvatarUriChangedEvent -> setState {
                        copy(
                            avatarUri = event.avatarUri,
                        )
                    }
                    is UiEvent.BannerUriChangedEvent -> setState {
                        copy(
                            bannerUri = event.bannerUri,
                        )
                    }
                    is UiEvent.DisplayNameChangedEvent -> setState {
                        copy(
                            displayName = event.name,
                        )
                    }
                    is UiEvent.UsernameChangedEvent -> setState { copy(username = event.handle) }
                    is UiEvent.LightningAddressChangedEvent -> setState {
                        copy(
                            lightningAddress = event.lightningAddress,
                        )
                    }
                    is UiEvent.Nip05IdentifierChangedEvent -> setState {
                        copy(
                            nip05Identifier = event.nip05Identifier,
                        )
                    }
                    is UiEvent.WebsiteChangedEvent -> setState { copy(website = event.website) }
                    is UiEvent.AboutMeChangedEvent -> setState { copy(aboutMe = event.aboutMe) }
                    is UiEvent.ToggleFollowEvent -> toggleFollow(event = event)
                    is UiEvent.ToggleGroupFollowEvent -> toggleGroupFollow(event = event)
                }
            }
        }

    private suspend fun createNostrAccount() {
        try {
            setState { copy(loading = true) }
            val userId = authRepository.createAccountAndLogin()
            val profile = state.value.asProfileMetadata()
            userRepository.setProfileMetadata(userId = userId, profileMetadata = profile)
            setState {
                copy(
                    userId = userId,
                    currentStep = UiState.CreateAccountStep.ACCOUNT_CREATED,
                )
            }
        } catch (e: UnsuccessfulFileUpload) {
            setState { copy(error = UiState.CreateError.FailedToUploadImage(e)) }
        } catch (e: NostrPublishException) {
            setState { copy(error = UiState.CreateError.FailedToCreateMetadata(e)) }
        } catch (e: WssException) {
            setState { copy(error = UiState.CreateError.FailedToCreateMetadata(e)) }
        } finally {
            setState { copy(loading = false) }
        }
    }

    private suspend fun fetchRecommendedFollows() {
        try {
            setState { copy(loading = true) }
            val response = recommendedFollowsApi.fetch(state.value.displayName)

            val result = response.suggestions.map { sg ->
                return@map sg.members.map { Pair(sg.group, it) }
            }.flatten().map {
                RecommendedFollow(
                    pubkey = it.second.pubkey,
                    groupName = it.first,
                    content = NostrJson.decodeFromString(
                        response.metadata[it.second.pubkey]!!.content,
                    ),
                    isCurrentUserFollowing = true,
                )
            }

            setState {
                copy(
                    recommendedFollows = result,
                    currentStep = UiState.CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS,
                )
            }
        } catch (e: IOException) {
            setState { copy(error = UiState.CreateError.FailedToFetchRecommendedFollows(e)) }
        } finally {
            setState { copy(loading = false) }
        }
    }

    private suspend fun finish() {
        try {
            setState { copy(loading = true) }
            val userId = state.value.userId!!
            profileRepository.setContactsAndRelays(
                userId = userId,
                contacts = state.value.recommendedFollows
                    .filter { it.isCurrentUserFollowing }
                    .map { it.pubkey }.toSet(),
                relays = BOOTSTRAP_RELAYS,
            )
            settingsRepository.fetchAndPersistAppSettings(userId = userId)
            setEffect(SideEffect.AccountCreatedAndPersisted(pubkey = userId))
        } catch (e: NostrPublishException) {
            setState { copy(error = UiState.CreateError.FailedToFollow(e)) }
        } catch (e: WssException) {
            setState { copy(error = UiState.CreateError.FailedToFollow(e)) }
        } finally {
            setState { copy(loading = false) }
        }
    }

    private fun goBack() {
        var step = state.value.currentStep.step - 1
        if (step <= 1) step = 1
        setState { copy(currentStep = UiState.CreateAccountStep(step)!!) }
    }

    private fun toggleFollow(event: UiEvent.ToggleFollowEvent) {
        val oldFollow =
            state.value.recommendedFollows.first {
                it.pubkey == event.pubkey && it.groupName == event.groupName
            }

        val index = state.value.recommendedFollows.indexOf(oldFollow)

        val newFollow =
            oldFollow.copy(isCurrentUserFollowing = !oldFollow.isCurrentUserFollowing)

        val newFollows = state.value.recommendedFollows.toMutableList()
        newFollows[index] = newFollow

        setState { copy(recommendedFollows = newFollows) }
    }

    private fun toggleGroupFollow(event: UiEvent.ToggleGroupFollowEvent) {
        val newFollows = state.value.recommendedFollows.toMutableList()

        val groupFollowState =
            state.value.recommendedFollows.filter { it.groupName == event.groupName }
                .any { !it.isCurrentUserFollowing }

        for (f in newFollows) {
            if (f.groupName == event.groupName) {
                newFollows[newFollows.indexOf(f)] =
                    f.copy(isCurrentUserFollowing = groupFollowState)
            }
        }

        setState { copy(recommendedFollows = newFollows) }
    }

    private fun UiState.asProfileMetadata(): ProfileMetadata =
        ProfileMetadata(
            displayName = this.displayName,
            username = this.username,
            website = this.website.ifEmpty { null },
            about = this.aboutMe.ifEmpty { null },
            localPictureUri = this.avatarUri,
            localBannerUri = this.bannerUri,
            lightningAddress = this.lightningAddress.ifEmpty { null },
            nostrVerification = this.nip05Identifier.ifEmpty { null },
        )
}

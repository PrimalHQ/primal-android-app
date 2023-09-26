package net.primal.android.profile.edit

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val userAccountStore: UserAccountsStore
) : ViewModel() {
    private val profileId: String = activeAccountStore.activeUserId()

    private val _state = MutableStateFlow(EditProfileContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: EditProfileContract.UiState.() -> EditProfileContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val _effect: Channel<EditProfileContract.SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: EditProfileContract.SideEffect) = viewModelScope.launch {
        _effect.send(effect)
    }

    private val _event: MutableSharedFlow<EditProfileContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: EditProfileContract.UiEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    init {
        observeEvents()
        observeActiveProfile()
        fetchLatestProfile()
    }

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is EditProfileContract.UiEvent.DisplayNameChangedEvent -> setState {
                    copy(
                        displayName = it.displayName
                    )
                }

                is EditProfileContract.UiEvent.NameChangedEvent -> setState { copy(name = it.name) }
                is EditProfileContract.UiEvent.AboutMeChangedEvent -> setState { copy(aboutMe = it.aboutMe) }
                is EditProfileContract.UiEvent.WebsiteChangedEvent -> setState { copy(website = it.website) }
                is EditProfileContract.UiEvent.LightningAddressChangedEvent -> setState {
                    copy(
                        lightningAddress = it.lightningAddress
                    )
                }

                is EditProfileContract.UiEvent.Nip05IdentifierChangedEvent -> setState {
                    copy(
                        nip05Identifier = it.nip05Identifier
                    )
                }

                is EditProfileContract.UiEvent.AvatarUriChangedEvent -> setState { copy(localAvatarUri = it.avatarUri, remoteAvatarUrl = null) }
                is EditProfileContract.UiEvent.BannerUriChangedEvent -> setState { copy(localBannerUri = it.bannerUri, remoteBannerUrl = null) }
                is EditProfileContract.UiEvent.SaveProfileEvent -> saveProfile()
            }
        }
    }

    private fun observeActiveProfile() = viewModelScope.launch {
        profileRepository.observeProfile(profileId = profileId).collect {
            setState {
                copy(
                    displayName = it.metadata?.displayName ?: "",
                    name = it.metadata?.handle ?: "",
                    aboutMe = it.metadata?.about ?: "",
                    website = it.metadata?.website ?: "",
                    lightningAddress = it.metadata?.lightningAddress ?: "",
                    nip05Identifier = it.metadata?.internetIdentifier ?: "",
                    remoteBannerUrl = it.metadata?.banner,
                    remoteAvatarUrl = it.metadata?.picture
                )
            }
        }
    }

    private fun fetchLatestProfile() = viewModelScope.launch {
        try {
            profileRepository.requestProfileUpdate(profileId = profileId)
        } catch (error: WssException) {
            // Ignore
        }
    }

    private suspend fun saveProfile() {
        setState { copy(loading = true) }
        try {
            val profileMetadata = state.value.toProfileMetadata()

            val updatedProfileMetadata = profileRepository.setProfileMetadata(
                userId = profileId,
                profileMetadata = profileMetadata
            )
            profileRepository.requestProfileUpdate(profileId = profileId)
            userAccountStore.getAndUpdateAccount(userId = profileId) {
                copy(pictureUrl = updatedProfileMetadata.remotePictureUrl)
            }
            setEffect(effect = EditProfileContract.SideEffect.AccountSuccessfulyEdited)
        } catch (error: NostrPublishException) {
            setErrorState(
                error = EditProfileContract.UiState.EditProfileError.FailedToPublishMetadata(
                    error
                )
            )
        } catch (error: UnsuccessfulFileUpload) {
            setErrorState(
                error = EditProfileContract.UiState.EditProfileError.FailedToUploadImage(
                    error
                )
            )
        } finally {
            setState { copy(loading = false) }
        }
    }

    private fun setErrorState(error: EditProfileContract.UiState.EditProfileError) {
        setState { copy(error = error) }
        viewModelScope.launch {
            delay(2.seconds)
            if (state.value.error == error) {
                setState { copy(error = null) }
            }
        }
    }
}

fun EditProfileContract.UiState.toProfileMetadata(): ProfileMetadata {
    return ProfileMetadata(
        displayName = this.displayName,
        handle = this.name,
        website = this.website,
        about = this.aboutMe,
        lightningAddress = this.lightningAddress,
        nostrVerification = this.nip05Identifier,
        localPictureUri = this.localAvatarUri,
        localBannerUri = this.localBannerUri,
        remotePictureUrl = this.remoteAvatarUrl,
        remoteBannerUrl = this.remoteBannerUrl
    )
}
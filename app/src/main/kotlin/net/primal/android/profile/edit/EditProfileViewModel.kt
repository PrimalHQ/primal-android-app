package net.primal.android.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.profile.edit.EditProfileContract.UiEvent
import net.primal.android.profile.edit.EditProfileContract.UiState.EditProfileError
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val profileId: String = activeAccountStore.activeUserId()

    private val _state = MutableStateFlow(EditProfileContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: EditProfileContract.UiState.() -> EditProfileContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val _effect: Channel<EditProfileContract.SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: EditProfileContract.SideEffect) =
        viewModelScope.launch {
            _effect.send(effect)
        }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch {
            events.emit(event)
        }
    }

    init {
        observeEvents()
        observeActiveProfile()
        fetchLatestProfile()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.DisplayNameChangedEvent -> setState {
                        copy(
                            displayName = it.displayName,
                        )
                    }
                    is UiEvent.UsernameChangedEvent -> setState { copy(username = it.name) }
                    is UiEvent.AboutMeChangedEvent -> setState { copy(aboutMe = it.aboutMe) }
                    is UiEvent.WebsiteChangedEvent -> setState { copy(website = it.website) }
                    is UiEvent.LightningAddressChangedEvent -> setState {
                        copy(
                            lightningAddress = it.lightningAddress,
                        )
                    }
                    is UiEvent.Nip05IdentifierChangedEvent -> setState {
                        copy(
                            nip05Identifier = it.nip05Identifier,
                        )
                    }
                    is UiEvent.AvatarUriChangedEvent -> setState {
                        copy(
                            localAvatarUri = it.avatarUri,
                            remoteAvatarUrl = null,
                        )
                    }

                    is UiEvent.BannerUriChangedEvent -> setState {
                        copy(
                            localBannerUri = it.bannerUri,
                            remoteBannerUrl = null,
                        )
                    }

                    is UiEvent.SaveProfileEvent -> saveProfile()
                }
            }
        }

    private fun observeActiveProfile() =
        viewModelScope.launch {
            profileRepository.observeProfile(profileId = profileId).collect {
                setState {
                    copy(
                        displayName = it.metadata?.displayName.orEmpty(),
                        username = it.metadata?.handle.orEmpty(),
                        aboutMe = it.metadata?.about.orEmpty(),
                        website = it.metadata?.website.orEmpty(),
                        lightningAddress = it.metadata?.lightningAddress.orEmpty(),
                        nip05Identifier = it.metadata?.internetIdentifier.orEmpty(),
                        remoteBannerUrl = it.metadata?.bannerCdnImage?.sourceUrl,
                        remoteAvatarUrl = it.metadata?.avatarCdnImage?.sourceUrl,
                    )
                }
            }
        }

    private fun fetchLatestProfile() =
        viewModelScope.launch {
            try {
                profileRepository.requestProfileUpdate(profileId = profileId)
            } catch (error: WssException) {
                // Ignore
            }
        }

    private suspend fun saveProfile() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                val profile = state.value.toProfileMetadata()
                userRepository.setProfileMetadata(userId = profileId, profileMetadata = profile)
                setEffect(effect = EditProfileContract.SideEffect.AccountSuccessfulyEdited)
            } catch (error: NostrPublishException) {
                setErrorState(error = EditProfileError.FailedToPublishMetadata(error))
            } catch (error: MissingRelaysException) {
                setErrorState(error = EditProfileError.MissingRelaysConfiguration(error))
            } catch (error: UnsuccessfulFileUpload) {
                setErrorState(error = EditProfileError.FailedToUploadImage(error))
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun setErrorState(error: EditProfileError) {
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
        username = this.username,
        website = this.website.ifEmpty { null },
        about = this.aboutMe.ifEmpty { null },
        lightningAddress = this.lightningAddress.ifEmpty { null },
        nostrVerification = this.nip05Identifier.ifEmpty { null },
        localPictureUri = this.localAvatarUri,
        localBannerUri = this.localBannerUri,
        remotePictureUrl = this.remoteAvatarUrl,
        remoteBannerUrl = this.remoteBannerUrl,
    )
}

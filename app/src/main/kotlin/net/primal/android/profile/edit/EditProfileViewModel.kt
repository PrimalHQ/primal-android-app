package net.primal.android.profile.edit

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.utils.userNameUiFriendly
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.db.authorNameUiFriendly
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val profileId: String = activeAccountStore.activeUserId()

    private val _state = MutableStateFlow(EditProfileContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: EditProfileContract.UiState.() -> EditProfileContract.UiState) {
        _state.getAndUpdate(reducer)
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
                is EditProfileContract.UiEvent.DisplayNameChangedEvent -> setState { copy(displayName = it.displayName) }
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

                is EditProfileContract.UiEvent.AvatarUriChangedEvent -> setState { copy(avatarUri = it.avatarUri) }
                is EditProfileContract.UiEvent.BannerUriChangedEvent -> setState { copy(bannerUri = it.bannerUri) }
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
                    bannerUri = it.metadata?.banner?.toUri(),
                    avatarUri = it.metadata?.picture?.toUri()
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
}
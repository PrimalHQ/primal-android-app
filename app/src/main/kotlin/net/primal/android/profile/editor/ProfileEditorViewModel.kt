package net.primal.android.profile.editor

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.utils.isPrimalIdentifier
import net.primal.android.networking.primal.upload.UnsuccessfulFileUpload
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.utils.hasPremiumMembership
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.profile.editor.ProfileEditorContract.SideEffect
import net.primal.android.profile.editor.ProfileEditorContract.UiEvent
import net.primal.android.profile.editor.ProfileEditorContract.UiState
import net.primal.android.profile.editor.ProfileEditorContract.UiState.EditProfileError
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.nwc.InvalidLud16Exception
import net.primal.android.wallet.nwc.LightningAddressChecker
import timber.log.Timber

@HiltViewModel
class ProfileEditorViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val lightningAddressChecker: LightningAddressChecker,
) : ViewModel() {

    private val profileId: String = activeAccountStore.activeUserId()

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

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

                    UiEvent.DismissError -> setState { copy(error = null) }
                    UiEvent.DismissPremiumPaywallDialog -> setState { copy(showPremiumPaywallDialog = false) }
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
                Timber.w(error)
            }
        }

    private fun saveProfile() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                val profile = state.value.toProfileMetadata()
                val nip05 = profile.nostrVerification
                if (!nip05.isNullOrEmpty()) {
                    if (!Patterns.EMAIL_ADDRESS.matcher(nip05).matches()) {
                        setErrorState(EditProfileError.InvalidNostrVerificationAddress(nip05 = nip05))
                        return@launch
                    }
                }

                val isActiveAccountPremium = activeAccountStore.activeUserAccount().hasPremiumMembership()
                if (profile.hasPrimalPremiumAddress() && !isActiveAccountPremium) {
                    setState { copy(showPremiumPaywallDialog = true) }
                } else {
                    withContext(dispatcherProvider.io()) {
                        val lud16 = profile.lightningAddress
                        if (!lud16.isNullOrEmpty()) {
                            lightningAddressChecker.validateLightningAddress(lud16 = lud16)
                        }

                        userRepository.setProfileMetadata(userId = profileId, profileMetadata = profile)
                    }
                    setEffect(effect = SideEffect.AccountSuccessfulyEdited)
                }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = EditProfileError.FailedToPublishMetadata(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = EditProfileError.MissingRelaysConfiguration(error))
            } catch (error: UnsuccessfulFileUpload) {
                Timber.w(error)
                setErrorState(error = EditProfileError.FailedToUploadImage(error))
            } catch (error: InvalidLud16Exception) {
                Timber.w(error)
                setErrorState(EditProfileError.InvalidLightningAddress(lud16 = error.lud16))
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun setErrorState(error: EditProfileError) {
        setState { copy(error = error) }
    }

    private fun UiState.toProfileMetadata(): ProfileMetadata {
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

    private fun ProfileMetadata.hasPrimalPremiumAddress() = this.nostrVerification.isPrimalIdentifier()
}

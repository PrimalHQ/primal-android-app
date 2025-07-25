package net.primal.android.premium.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.premium.domain.MembershipError
import net.primal.android.premium.home.PremiumHomeContract.UiEvent
import net.primal.android.premium.home.PremiumHomeContract.UiState
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.profile.ProfileRepository
import timber.log.Timber

@HiltViewModel
class PremiumHomeViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeActiveAccount()
        observeProfile()
        fetchShouldShowSupport()
        markPremiumHomeOpened()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.DismissError -> setState { copy(error = null) }
                    UiEvent.ApplyPrimalLightningAddress -> applyPrimalLightningAddress()
                    UiEvent.ApplyPrimalNostrAddress -> applyPrimalNostrAddress()
                    UiEvent.RequestMembershipUpdate -> fetchMembershipStatus()
                }
            }
        }
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        profileId = it.pubkey,
                        avatarCdnImage = it.avatarCdnImage,
                        profileNostrAddress = it.internetIdentifier,
                        profileLightningAddress = it.lightningAddress,
                        membership = it.premiumMembership,
                    )
                }
            }
        }

    private fun observeProfile() {
        viewModelScope.launch {
            profileRepository.observeProfileData(profileId = activeAccountStore.activeUserId()).collect {
                setState {
                    copy(
                        avatarLegendaryCustomization = it.primalPremiumInfo
                            ?.legendProfile?.asLegendaryCustomization(),
                    )
                }
            }
        }
    }

    private fun markPremiumHomeOpened() {
        viewModelScope.launch {
            userRepository.updateUpgradeDotTimestamp(userId = activeAccountStore.activeUserId())
        }
    }

    private fun applyPrimalNostrAddress() =
        viewModelScope.launch {
            val premiumMembership = state.value.membership ?: return@launch
            val nip05 = premiumMembership.nostrAddress

            try {
                userRepository.setNostrAddress(userId = activeAccountStore.activeUserId(), nostrAddress = nip05)
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(error = MembershipError.ProfileMetadataNotFound) }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState { copy(error = MembershipError.FailedToApplyNostrAddress) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = MembershipError.FailedToApplyNostrAddress) }
            }
        }

    private fun applyPrimalLightningAddress() =
        viewModelScope.launch {
            val premiumMembership = state.value.membership ?: return@launch
            val lud16 = premiumMembership.lightningAddress

            try {
                userRepository.setLightningAddress(userId = activeAccountStore.activeUserId(), lightningAddress = lud16)
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(error = MembershipError.ProfileMetadataNotFound) }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState { copy(error = MembershipError.FailedToApplyLightningAddress) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = MembershipError.FailedToApplyLightningAddress) }
            }
        }

    private fun fetchMembershipStatus() =
        viewModelScope.launch {
            try {
                premiumRepository.fetchMembershipStatus(activeAccountStore.activeUserId())
            } catch (error: NetworkException) {
                Timber.w(error)
            }
        }

    private fun fetchShouldShowSupport() =
        viewModelScope.launch {
            try {
                val clientConfigShowSupport = premiumRepository.shouldShowSupportUsNotice()
                setState {
                    copy(
                        showSupportUsNotice = clientConfigShowSupport,
                    )
                }
            } catch (error: NetworkException) {
                Timber.e(error)
            }
        }
}

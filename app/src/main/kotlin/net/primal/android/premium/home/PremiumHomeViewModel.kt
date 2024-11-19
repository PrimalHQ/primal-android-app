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
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.domain.MembershipError
import net.primal.android.premium.home.PremiumHomeContract.UiEvent
import net.primal.android.premium.home.PremiumHomeContract.UiState
import net.primal.android.premium.legend.LegendaryProfile
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.premium.utils.isPrimalLegend
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
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
            profileRepository.observeProfile(profileId = activeAccountStore.activeUserId()).collect {
                setState {
                    copy(
                        avatarGlow = it.metadata?.primalLegendProfile?.avatarGlow == true,
                        customBadge = it.metadata?.primalLegendProfile?.customBadge == true,
                        legendaryProfile = LegendaryProfile.valueById(it.metadata?.primalLegendProfile?.styleId)
                            ?: LegendaryProfile.NO_CUSTOMIZATION,
                    )
                }
            }
        }
    }

    private fun applyPrimalNostrAddress() =
        viewModelScope.launch {
            val premiumMembership = state.value.membership ?: return@launch
            val nip05 = premiumMembership.nostrAddress

            try {
                userRepository.setNostrAddress(userId = activeAccountStore.activeUserId(), nostrAddress = nip05)
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = MembershipError.ProfileMetadataNotFound) }
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
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = MembershipError.ProfileMetadataNotFound) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = MembershipError.FailedToApplyLightningAddress) }
            }
        }

    private fun fetchMembershipStatus() =
        viewModelScope.launch {
            try {
                premiumRepository.fetchMembershipStatus(activeAccountStore.activeUserId())
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun fetchShouldShowSupport() =
        viewModelScope.launch {
            try {
                val clientConfigShowSupport = premiumRepository.shouldShowSupportUsNotice()
                setState {
                    copy(
                        showSupportUsNotice = clientConfigShowSupport &&
                            membership?.cohort1?.isPrimalLegend() == false,
                    )
                }
            } catch (error: WssException) {
                Timber.e(error)
            }
        }
}

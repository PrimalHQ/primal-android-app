package net.primal.android.premium.legend.customization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.api.model.UpdatePrimalLegendProfileRequest
import net.primal.android.premium.legend.customization.LegendaryProfileCustomizationContract.UiEvent
import net.primal.android.premium.legend.customization.LegendaryProfileCustomizationContract.UiState
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import timber.log.Timber

@HiltViewModel
class LegendaryProfileCustomizationViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val premiumRepository: PremiumRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeActiveAccount()
        observeProfile()
        observeEvents()
        requestProfileUpdate()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.ApplyCustomization -> applyCustomization(it)
                }
            }
        }
    }

    private fun applyCustomization(event: UiEvent.ApplyCustomization) {
        viewModelScope.launch {
            setState { copy(applyingChanges = true) }
            event.optimisticallyUpdateCustomization()

            try {
                premiumRepository.updateLegendProfile(
                    userId = activeAccountStore.activeUserId(),
                    updateProfileRequest = UpdatePrimalLegendProfileRequest(
                        styleId = event.style?.id,
                        avatarGlow = event.avatarGlow,
                        customBadge = event.customBadge,
                        inLeaderboard = event.inLeaderboard,
                        editedShoutout = event.editedShoutout,
                    ),
                )
            } catch (error: WssException) {
                Timber.e(error)
            } finally {
                runCatching { userRepository.fetchAndUpdateUserAccount(userId = activeAccountStore.activeUserId()) }
                setState { copy(applyingChanges = false) }
            }
        }
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        avatarCdnImage = it.avatarCdnImage,
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
                        avatarLegendaryCustomization = it.metadata?.primalPremiumInfo
                            ?.legendProfile?.asLegendaryCustomization() ?: LegendaryCustomization(),
                    )
                }
            }
        }
    }

    private fun requestProfileUpdate() {
        viewModelScope.launch {
            profileRepository.requestProfileUpdate(profileId = activeAccountStore.activeUserId())
        }
    }

    private fun UiEvent.ApplyCustomization.optimisticallyUpdateCustomization() {
        val data = this
        setState {
            copy(
                avatarLegendaryCustomization = avatarLegendaryCustomization.copy(
                    avatarGlow = data.avatarGlow ?: avatarLegendaryCustomization.avatarGlow,
                    customBadge = data.customBadge ?: avatarLegendaryCustomization.customBadge,
                    legendaryStyle = data.style ?: avatarLegendaryCustomization.legendaryStyle,
                    inLeaderboard = data.inLeaderboard ?: avatarLegendaryCustomization.inLeaderboard,
                ),
            )
        }
    }
}

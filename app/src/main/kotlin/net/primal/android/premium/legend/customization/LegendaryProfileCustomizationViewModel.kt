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
import net.primal.android.nostr.notary.exceptions.SignException
import net.primal.android.premium.api.model.UpdatePrimalLegendProfileRequest
import net.primal.android.premium.legend.customization.LegendaryProfileCustomizationContract.UiEvent
import net.primal.android.premium.legend.customization.LegendaryProfileCustomizationContract.UiState
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.core.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel
class LegendaryProfileCustomizationViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val premiumRepository: PremiumRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        requestUpdateActiveAccount()
        observeActiveAccount()
        observeEvents()
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
            } catch (error: SignException) {
                Timber.e(error)
            } catch (error: WssException) {
                Timber.e(error)
            } finally {
                requestUpdateActiveAccount()
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
                        avatarLegendaryCustomization = it.primalLegendProfile?.asLegendaryCustomization()
                            ?: LegendaryCustomization(),
                    )
                }
            }
        }

    private fun requestUpdateActiveAccount() =
        viewModelScope.launch {
            runCatching {
                userRepository.fetchAndUpdateUserAccount(userId = activeAccountStore.activeUserId())
                premiumRepository.fetchMembershipStatus(userId = activeAccountStore.activeUserId())
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

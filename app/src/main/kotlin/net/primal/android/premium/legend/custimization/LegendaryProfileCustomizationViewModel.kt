package net.primal.android.premium.legend.custimization

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
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.legend.LegendaryStyle
import net.primal.android.premium.legend.custimization.LegendaryProfileCustomizationContract.SideEffect
import net.primal.android.premium.legend.custimization.LegendaryProfileCustomizationContract.UiEvent
import net.primal.android.premium.legend.custimization.LegendaryProfileCustomizationContract.UiState
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.profile.domain.PrimalLegendProfile
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel
class LegendaryProfileCustomizationViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val premiumRepository: PremiumRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

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

            try {
                premiumRepository.updateLegendProfile(
                    userId = activeAccountStore.activeUserId(),
                    profile = PrimalLegendProfile(
                        avatarGlow = event.avatarGlow,
                        customBadge = event.customBadge,
                        styleId = event.style.id,
                    ),
                )
                profileRepository.requestProfileUpdate(profileId = activeAccountStore.activeUserId())
                setEffect(SideEffect.CustomizationSaved)
            } catch (error: WssException) {
                Timber.e(error)
            } finally {
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
                        avatarGlow = it.metadata?.primalLegendProfile?.avatarGlow == true,
                        customBadge = it.metadata?.primalLegendProfile?.customBadge == true,
                        legendaryStyle = LegendaryStyle.valueById(it.metadata?.primalLegendProfile?.styleId)
                            ?: LegendaryStyle.NO_CUSTOMIZATION,
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
}

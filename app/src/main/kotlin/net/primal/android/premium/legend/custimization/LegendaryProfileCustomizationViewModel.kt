package net.primal.android.premium.legend.custimization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.premium.legend.LegendaryProfile
import net.primal.android.premium.legend.custimization.LegendaryProfileCustomizationContract.UiState
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class LegendaryProfileCustomizationViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        observeActiveAccount()
        observeProfile()
        requestProfileUpdate()
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
                        legendaryProfile = LegendaryProfile.valueById(it.metadata?.primalLegendProfile?.styleId)
                            ?: LegendaryProfile.NO_CUSTOMIZATION,
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

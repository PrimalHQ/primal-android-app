package net.primal.android.premium.card

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.navigation.profileIdOrThrow
import net.primal.android.premium.utils.isPrimalLegendTier
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class PremiumCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {
    private val profileId = savedStateHandle.profileIdOrThrow

    private val _state = MutableStateFlow(PremiumCardContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: PremiumCardContract.UiState.() -> PremiumCardContract.UiState) =
        _state.getAndUpdate { it.reducer() }

    init {
        viewModelScope.launch {
            runCatching { profileRepository.requestProfileUpdate(profileId = profileId) }
        }
        observeActiveAccount()
        observeProfileById(profileId)
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        isActiveAccountCard = it.pubkey == profileId,
                        isActiveAccountLegend = it.premiumMembership?.isPrimalLegendTier() == true,
                    )
                }
            }
        }

    private fun observeProfileById(profileId: String) =
        viewModelScope.launch {
            profileRepository.observeProfileData(profileId = profileId)
                .collect { profile ->
                    setState {
                        copy(
                            profile = profile.asProfileDetailsUi(),
                            isPrimalLegend = profile.primalPremiumInfo?.legendProfile != null,
                        )
                    }
                }
        }
}

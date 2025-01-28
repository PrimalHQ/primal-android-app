package net.primal.android.premium.legend.card

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.navigation.profileIdOrThrow
import net.primal.android.premium.legend.card.LegendCardContract.UiState
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class LegendCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {
    private val profileId = savedStateHandle.profileIdOrThrow

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        viewModelScope.launch { profileRepository.requestProfileUpdate(profileId = profileId) }
        observeActiveAccount()
        observeProfileById(profileId)
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState { copy(isActiveAccountCard = it.pubkey == profileId) }
            }
        }

    private fun observeProfileById(profileId: String) =
        viewModelScope.launch {
            profileRepository.observeProfileData(profileId = profileId)
                .collect { profile ->
                    setState { copy(profile = profile.asProfileDetailsUi()) }
                }
        }
}

package net.primal.android.messages.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.messages.list.MessageListContract.UiState
import net.primal.android.user.badges.BadgesManager
import net.primal.android.user.accounts.active.ActiveAccountStore
import javax.inject.Inject

@HiltViewModel
class MessageListViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val badgesManager: BadgesManager,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        subscribeToActiveAccount()
        subscribeToBadgesUpdates()
    }

    private fun subscribeToActiveAccount() = viewModelScope.launch {
        activeAccountStore.activeUserAccount.collect {
            setState {
                copy(activeAccountAvatarUrl = it.pictureUrl)
            }
        }
    }

    private fun subscribeToBadgesUpdates() = viewModelScope.launch {
        badgesManager.badges.collect {
            setState {
                copy(badges = it)
            }
        }
    }
}

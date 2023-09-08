package net.primal.android.notifications.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.notifications.list.NotificationsContract.UiState
import net.primal.android.notifications.repository.NotificationRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val repository: NotificationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        subscribeToActiveAccount()

        viewModelScope.launch {
            repository.fetchNotificationsSummary(userId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b")
        }
    }

    private fun subscribeToActiveAccount() = viewModelScope.launch {
        activeAccountStore.activeAccountState
            .filterIsInstance<ActiveUserAccountState.ActiveUserAccount>()
            .collect {
                setState {
                    copy(
                        activeAccountAvatarUrl = it.data.pictureUrl,
                    )
                }
            }
    }
}

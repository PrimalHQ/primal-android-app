package net.primal.android.notifications.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.notifications.list.NotificationsContract.UiEvent
import net.primal.android.notifications.list.NotificationsContract.UiState
import net.primal.android.notifications.repository.NotificationRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.badges.BadgesManager
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val notificationsRepository: NotificationRepository,
    private val badgesManager: BadgesManager,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { _event.emit(event) }

    init {
        subscribeToEvents()
        subscribeToActiveAccount()
        observeNotifications()
        subscribeToBadgesUpdates()
    }

    private fun subscribeToEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                UiEvent.NotificationsSeen -> handleNotificationsSeen()
            }
        }
    }

    private fun subscribeToActiveAccount() = viewModelScope.launch {
        activeAccountStore.activeUserAccount.collect {
            setState {
                copy(activeAccountAvatarUrl = it.pictureUrl)
            }
        }
    }

    private fun observeNotifications() = viewModelScope.launch {
        notificationsRepository.observeNotifications().collect {
            setState { copy(notifications = it) }
        }
    }

    private fun subscribeToBadgesUpdates() = viewModelScope.launch {
        badgesManager.badges.collect {
            setState { copy(badges = it) }
            if (it.notifications > 0) {
                fetchNotifications()
            } else if (notificationsRepository.countNotifications() == 0) {
                fetchNotifications()
            }
        }
    }

    private fun fetchNotifications() = viewModelScope.launch {
        setState { copy(loading = true) }
        try {
            val activeUserId = activeAccountStore.activeUserId()
            notificationsRepository.deleteNotifications(userId = activeUserId)
            notificationsRepository.fetchNotifications(userId = activeUserId)
        } finally {
            setState { copy(loading = false) }
        }
    }

    private fun handleNotificationsSeen() = viewModelScope.launch {
        val activeUserId = activeAccountStore.activeUserId()
        notificationsRepository.updateLastSeenTimestamp(userId = activeUserId)
    }
}

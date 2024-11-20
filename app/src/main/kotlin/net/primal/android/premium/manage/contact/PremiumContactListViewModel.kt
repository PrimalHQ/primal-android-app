package net.primal.android.premium.manage.contact

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
import net.primal.android.premium.manage.contact.PremiumContactListContract.UiEvent
import net.primal.android.premium.manage.contact.PremiumContactListContract.UiState
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel
class PremiumContactListViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val premiumRepository: PremiumRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchContactsLists()
    }

    private fun fetchContactsLists() {
        viewModelScope.launch {
            setState { copy(fetching = true) }
            try {
                premiumRepository.fetchRecoveryContactsList(
                    userId = activeAccountStore.activeUserId(),
                )
            } catch (error: WssException) {
                Timber.e(error)
            } finally {
                setState { copy(fetching = false) }
            }
        }
    }
}

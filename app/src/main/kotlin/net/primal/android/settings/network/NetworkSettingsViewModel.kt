package net.primal.android.settings.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.config.AppConfigProvider
import net.primal.android.settings.network.NetworkSettingsContract.UiEvent
import net.primal.android.settings.network.NetworkSettingsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class NetworkSettingsViewModel @Inject constructor(
    private val appConfigProvider: AppConfigProvider,
    private val activeUserAccountsStore: ActiveAccountStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _uiState.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeActiveAccount()
        observeCachingServiceUrl()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.RestoreDefaultRelays -> Unit
                    is UiEvent.DeleteRelay -> Unit
                    is UiEvent.AddRelay -> Unit
                }
            }
        }

    private fun observeCachingServiceUrl() =
        viewModelScope.launch {
            appConfigProvider.cacheUrl().collect {
                setState { copy(cachingUrl = it) }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeUserAccountsStore.activeUserAccount.collect {
                setState { copy(relays = it.relays) }
            }
        }
}

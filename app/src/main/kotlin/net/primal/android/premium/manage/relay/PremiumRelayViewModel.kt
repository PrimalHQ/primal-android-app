package net.primal.android.premium.manage.relay

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
import net.primal.android.nostr.notary.MissingPrivateKeyException
import net.primal.android.premium.manage.relay.PremiumRelayContract.UiEvent
import net.primal.android.premium.manage.relay.PremiumRelayContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.RelayRepository
import timber.log.Timber

@HiltViewModel
class PremiumRelayViewModel @Inject constructor(
    private val relayRepository: RelayRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observePremiumRelay()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.ConnectToRelay -> connectToRelay()
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun observePremiumRelay() =
        viewModelScope.launch {
            relayRepository.observeUserRelays(activeAccountStore.activeUserId()).collect {
                setState { copy(isConnected = it.map { it.url }.contains(state.value.relayUrl)) }
            }
        }

    private fun connectToRelay() =
        viewModelScope.launch {
            setState { copy(addingRelay = true) }
            try {
                relayRepository.addRelayAndPublishRelayList(
                    userId = activeAccountStore.activeUserId(),
                    url = state.value.relayUrl,
                )
            } catch (error: MissingPrivateKeyException) {
                Timber.w(error)
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = error) }
            } finally {
                setState { copy(addingRelay = false) }
            }
        }
}

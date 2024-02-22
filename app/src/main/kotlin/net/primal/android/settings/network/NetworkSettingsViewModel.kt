package net.primal.android.settings.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.relays.RelaysSocketManager
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.settings.network.NetworkSettingsContract.UiEvent
import net.primal.android.settings.network.NetworkSettingsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.RelayRepository
import timber.log.Timber

@HiltViewModel
class NetworkSettingsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val relaysSocketManager: RelaysSocketManager,
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
    private val relayRepository: RelayRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _uiState.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private var latestRelaysPoolStatus = emptyMap<String, Boolean>()

    init {
        observeEvents()
        ensureRelayPoolUpdatedAndConnected()
        observeRelayPoolConnections()
        observeCachingServiceConnection()
        observeUserRelays()
    }

    private fun observeUserRelays() =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            relayRepository.observeUserRelays(userId).collect {
                setState {
                    copy(
                        relays = it.map {
                            SocketDestinationUiState(
                                url = it.url,
                                connected = latestRelaysPoolStatus[it.url] ?: false,
                            )
                        },
                    )
                }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.RestoreDefaultRelays -> restoreDefaultRelays()
                    is UiEvent.DeleteRelay -> deleteRelay(it.url)
                    is UiEvent.ConfirmAddRelay -> addRelay(it.url)
                    is UiEvent.UpdateNewRelayUrl -> setState { copy(newRelayUrl = it.url) }
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun ensureRelayPoolUpdatedAndConnected() =
        viewModelScope.launch {
            try {
                relayRepository.fetchAndUpdateUserRelays(userId = activeAccountStore.activeUserId())
            } catch (error: WssException) {
                Timber.w(error)
            }
            delay(1.seconds)
            relaysSocketManager.ensureUserRelayPoolConnected()
        }

    private fun ensureRelayConnected(url: String) =
        viewModelScope.launch {
            delay(1.seconds)
            relaysSocketManager.ensureUserRelayConnected(url)
        }

    private fun observeCachingServiceConnection() =
        viewModelScope.launch {
            primalApiClient.connectionStatus.collect {
                setState { copy(cachingService = SocketDestinationUiState(url = it.url, it.connected)) }
            }
        }

    private fun observeRelayPoolConnections() =
        viewModelScope.launch {
            relaysSocketManager.userRelayPoolStatus.collect { poolStatus ->
                latestRelaysPoolStatus = poolStatus
                setState {
                    copy(
                        relays = this.relays.toMutableList().apply {
                            forEachIndexed { index, relay ->
                                this[index] = relay.copy(connected = poolStatus[relay.url] ?: false)
                            }
                        },
                    )
                }
            }
        }

    private fun restoreDefaultRelays() =
        viewModelScope.launch {
            changeRelayList { userId ->
                relayRepository.bootstrapDefaultUserRelays(userId = userId)
                ensureRelayPoolUpdatedAndConnected()
            }
        }

    private fun deleteRelay(url: String) =
        viewModelScope.launch {
            changeRelayList { userId ->
                relayRepository.removeRelayAndPublishRelayList(userId = userId, url = url)
            }
        }

    private fun addRelay(url: String) =
        viewModelScope.launch {
            changeRelayList { userId ->
                relayRepository.addRelayAndPublishRelayList(userId = userId, url = url)
                ensureRelayConnected(url)
                setState { copy(newRelayUrl = "") }
            }
        }

    private suspend fun changeRelayList(block: suspend (String) -> Unit) {
        try {
            setState { copy(working = true) }
            val userId = activeAccountStore.activeUserId()
            block(userId)
        } catch (error: WssException) {
            Timber.w(error)
            setState { copy(error = UiState.NetworkSettingsError.FailedToAddRelay(error)) }
        } catch (error: NostrPublishException) {
            Timber.w(error)
            setState { copy(error = UiState.NetworkSettingsError.FailedToAddRelay(error)) }
        } finally {
            setState { copy(working = false) }
        }
    }
}

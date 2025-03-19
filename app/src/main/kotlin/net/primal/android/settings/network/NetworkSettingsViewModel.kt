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
import net.primal.android.networking.relays.RelaysSocketManager
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.notary.MissingPrivateKeyException
import net.primal.android.settings.network.NetworkSettingsContract.UiEvent
import net.primal.android.settings.network.NetworkSettingsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.RelayRepository
import net.primal.android.user.repository.UserRepository
import net.primal.core.config.AppConfigHandler
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel
class NetworkSettingsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val relaysSocketManager: RelaysSocketManager,
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
    private val relayRepository: RelayRepository,
    private val appConfigHandler: AppConfigHandler,
    private val userRepository: UserRepository,
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
        observeCachingProxy()
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

    private fun observeCachingProxy() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect { userAccount ->
                setState {
                    copy(cachingProxyEnabled = userAccount.cachingProxyEnabled)
                }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.RestoreDefaultRelays -> restoreDefaultRelays()
                    UiEvent.RestoreDefaultCachingService -> restoreDefaultCachingService()
                    is UiEvent.DeleteRelay -> deleteRelay(url = it.url)
                    is UiEvent.ConfirmRelayInsert -> addRelay(url = it.url)
                    is UiEvent.UpdateNewRelayUrl -> setState { copy(newRelayUrl = it.url) }
                    is UiEvent.ConfirmCachingServiceChange -> changeCachingService(url = it.url)
                    is UiEvent.UpdateNewCachingServiceUrl -> setState { copy(newCachingServiceUrl = it.url) }
                    is UiEvent.UpdateCachingProxyFlag -> updateCachingServiceFlag(enabled = it.enabled)
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
                try {
                    relayRepository.bootstrapUserRelays(userId = userId)
                } catch (error: MissingPrivateKeyException) {
                    Timber.w(error)
                } catch (error: NostrPublishException) {
                    Timber.w(error)
                }
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
            setState { copy(updatingRelays = true) }
            val userId = activeAccountStore.activeUserId()
            block(userId)
        } catch (error: WssException) {
            Timber.w(error)
            setState { copy(error = UiState.NetworkSettingsError.FailedToAddRelay(error)) }
        } catch (error: MissingPrivateKeyException) {
            Timber.w(error)
            setState { copy(error = UiState.NetworkSettingsError.FailedToAddRelay(error)) }
        } catch (error: NostrPublishException) {
            Timber.w(error)
            setState { copy(error = UiState.NetworkSettingsError.FailedToAddRelay(error)) }
        } finally {
            setState { copy(updatingRelays = false) }
        }
    }

    private fun changeCachingService(url: String) =
        viewModelScope.launch {
            appConfigHandler.overrideCacheUrl(url = url)
            setState { copy(newCachingServiceUrl = "") }
        }

    private fun restoreDefaultCachingService() =
        viewModelScope.launch {
            appConfigHandler.restoreDefaultCacheUrl()
        }

    private fun updateCachingServiceFlag(enabled: Boolean) =
        viewModelScope.launch {
            userRepository.updateCachingProxyEnabled(
                userId = activeAccountStore.activeUserId(),
                enabled = enabled,
            )
            setState { copy(cachingProxyEnabled = enabled) }
        }
}

package net.primal.android.premium.manage.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.nostr.ext.takeContentOrNull
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.nostr.notary.exceptions.MissingPrivateKey
import net.primal.android.nostr.notary.exceptions.SignException
import net.primal.android.premium.manage.content.PremiumContentBackupContract.UiEvent
import net.primal.android.premium.manage.content.PremiumContentBackupContract.UiState
import net.primal.android.premium.manage.content.api.model.BroadcastingStatus
import net.primal.android.premium.manage.content.model.ContentGroup
import net.primal.android.premium.manage.content.model.ContentType
import net.primal.android.premium.manage.content.repository.BroadcastRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.primal.PrimalSocketSubscription
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.networking.utils.retryNetworkCall
import net.primal.core.utils.serialization.CommonJson
import net.primal.data.remote.model.AppSpecificDataRequest
import net.primal.domain.nostr.NostrEventKind
import timber.log.Timber

@HiltViewModel
class PremiumContentBackupViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val broadcastRepository: BroadcastRepository,
    @PrimalCacheApiClient private val primalCachingApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private var monitorBroadcasting: PrimalSocketSubscription<BroadcastingStatus>? = null
    private var monitorMutex = Mutex()

    init {
        observeEvents()
        fetchContentStats()
        fetchBroadcastStatus()
    }

    private fun fetchBroadcastStatus() {
        viewModelScope.launch {
            try {
                val status = retryNetworkCall(retries = 2) {
                    broadcastRepository.fetchBroadcastStatus(userId = activeAccountStore.activeUserId())
                }
                handleBroadcastStatus(status)
            } catch (error: WssException) {
                Timber.e(error)
            } catch (error: SignException) {
                Timber.e(error)
            }
        }
    }

    private fun fetchContentStats() {
        viewModelScope.launch {
            try {
                val stats = broadcastRepository.fetchContentStats(userId = activeAccountStore.activeUserId())
                setState {
                    copy(
                        contentTypes = ContentGroup.entries.map { group ->
                            ContentType(
                                group = group,
                                count = when (group) {
                                    ContentGroup.All -> stats.values.sum()
                                    else -> stats.filter { it.key in (group.kinds ?: emptyList()) }.values.sum()
                                },
                            )
                        },
                    )
                }
            } catch (error: WssException) {
                Timber.e(error)
            } catch (error: SignException) {
                Timber.e(error)
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.StartBroadcastingMonitor -> startBroadcastMonitorIfStopped()
                    UiEvent.StopBroadcastingMonitor -> stopBroadcastMonitor()
                    UiEvent.StopBroadcasting -> stopBroadcasting()
                    is UiEvent.StartBroadcasting -> startBroadcasting(type = it.type)
                }
            }
        }
    }

    private fun subscribeToBroadcastMonitor(userId: String) =
        PrimalSocketSubscription.launch(
            scope = viewModelScope,
            primalApiClient = primalCachingApiClient,
            cacheFilter = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.MEMBERSHIP_MONITOR_CONTENT_BROADCAST_STATUS.id,
                optionsJson = CommonJson.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = "",
                        ),
                    ),
                ),
            ),
            transformer = {
                if (primalEvent?.kind == NostrEventKind.PrimalContentBroadcastStatus.value) {
                    primalEvent.takeContentOrNull<BroadcastingStatus>()
                } else {
                    null
                }
            },
        ) { status -> handleBroadcastStatus(status) }

    private fun handleBroadcastStatus(status: BroadcastingStatus) {
        setState {
            val newContentTypes = this.contentTypes.toMutableList().apply {
                val index = this.indexOfFirst { it.group.kinds == status.kinds }
                if (index != -1) {
                    this[index] = this[index].copy(broadcasting = status.running, progress = status.progress)
                }
            }
            copy(
                contentTypes = newContentTypes,
                anyBroadcasting = newContentTypes.any { it.broadcasting },
            )
        }
    }

    private fun startBroadcastMonitorIfStopped() {
        viewModelScope.launch {
            monitorMutex.withLock {
                if (monitorBroadcasting == null) {
                    try {
                        monitorBroadcasting = subscribeToBroadcastMonitor(userId = activeAccountStore.activeUserId())
                    } catch (error: SignException) {
                        Timber.w(error)
                    }
                }
            }
        }
    }

    private fun stopBroadcastMonitor() {
        viewModelScope.launch {
            monitorMutex.withLock {
                monitorBroadcasting?.unsubscribe()
                monitorBroadcasting = null
            }
        }
    }

    private fun startBroadcasting(type: ContentType) {
        viewModelScope.launch {
            try {
                broadcastRepository.startBroadcast(
                    userId = activeAccountStore.activeUserId(),
                    kinds = type.group.kinds,
                )
                setState {
                    copy(
                        anyBroadcasting = true,
                        contentTypes = this.contentTypes.toMutableList().apply {
                            val index = this.indexOf(type)
                            if (index != -1) {
                                this[index] = this[index].copy(broadcasting = true, progress = 0.01f)
                            }
                        },
                    )
                }
            } catch (error: WssException) {
                Timber.e(error)
            } catch (error: SignException) {
                Timber.e(error)
            }
        }
    }

    private fun stopBroadcasting() {
        viewModelScope.launch {
            try {
                broadcastRepository.cancelBroadcast(userId = activeAccountStore.activeUserId())
            } catch (error: WssException) {
                Timber.e(error)
            } catch (error: SignException) {
                Timber.e(error)
            }
        }
    }
}

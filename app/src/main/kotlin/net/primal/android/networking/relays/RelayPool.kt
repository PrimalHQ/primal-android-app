package net.primal.android.networking.relays

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.android.networking.UserAgentProvider
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.NostrIncomingMessage
import net.primal.android.networking.sockets.NostrSocketClient
import net.primal.android.networking.sockets.errors.NostrNoticeException
import net.primal.android.networking.sockets.filterByEventId
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.serialization.toJsonObject
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.toRelay
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class RelayPool @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val activeAccountStore: ActiveAccountStore,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val poolMutex = Mutex()

    private var regularRelays = listOf<NostrSocketClient>()
    private var walletRelays = listOf<NostrSocketClient>()

    init {
        observeActiveAccount()
    }

    private fun observeActiveAccount() = scope.launch {
        activeAccountStore.activeAccountState.collect { activeAccountState ->
            when (activeAccountState) {
                is ActiveUserAccountState.ActiveUserAccount -> {
                    val data = activeAccountState.data
                    val regularRelays = data.relays
                    val walletRelays = data.nostrWallet?.relays?.map { it.toRelay() } ?: emptyList()
                    createSocketsPool(
                        regularRelays = regularRelays,
                        walletRelays = walletRelays,
                    )
                }

                ActiveUserAccountState.NoUserAccount -> {
                    clearSocketsPool()
                }
            }
        }
    }

    @Throws(NostrPublishException::class)
    suspend fun publishEvent(nostrEvent: NostrEvent) {
        when (NostrEventKind.valueOf(nostrEvent.kind)) {
            NostrEventKind.ZapRequest,
            NostrEventKind.WalletRequest -> {
                handlePublishEvent(walletRelays, nostrEvent)
            }
            else -> {
                handlePublishEvent(regularRelays, nostrEvent)
            }
        }
    }

    private fun Relay.toWssRequestOrNull() = try {
        Request.Builder()
            .url(url)
            .addHeader("User-Agent", UserAgentProvider.USER_AGENT)
            .build()
    } catch (error: IllegalArgumentException) {
        null
    }

    private suspend fun createSocketsPool(
        regularRelays: List<Relay>,
        walletRelays: List<Relay>
    ) {
        clearSocketsPool()
        poolMutex.withLock {
            this.regularRelays = regularRelays.mapAsNostrSocketClient()
            this.walletRelays = walletRelays.mapAsNostrSocketClient()
        }
    }

    private fun List<Relay>.mapAsNostrSocketClient() = this
        .mapNotNull { it.toWssRequestOrNull() }
        .map {
            NostrSocketClient(
                okHttpClient = okHttpClient,
                wssRequest = it
            )
        }

    private suspend fun clearSocketsPool() = poolMutex.withLock {
        regularRelays.forEach { it.close() }
        regularRelays = emptyList()
        walletRelays.forEach { it.close() }
        walletRelays = emptyList()
    }

    private fun NostrPublishResult.isSuccessful(): Boolean {
        return result is NostrIncomingMessage.OkMessage && result.success
    }

    @FlowPreview
    private suspend fun NostrSocketClient.collectPublishResponse(eventId: String): NostrIncomingMessage.OkMessage {
        return incomingMessages
            .filterByEventId(id = eventId)
            .transform {
                when (it) {
                    is NostrIncomingMessage.OkMessage -> emit(it)
                    is NostrIncomingMessage.NoticeMessage -> throw NostrNoticeException(reason = it.message)
                    else -> throw IllegalStateException("$it is not allowed")
                }
            }
            .timeout(30.seconds)
            .first()
    }

    @OptIn(FlowPreview::class)
    private suspend fun handlePublishEvent(
        relayConnections: List<NostrSocketClient>,
        nostrEvent: NostrEvent
    ) {
        val responseFlow = MutableSharedFlow<NostrPublishResult>()
        relayConnections.forEach { nostrSocketClient ->
            scope.launch {
                with(nostrSocketClient) {
                    ensureSocketConnection()
                    sendEVENT(nostrEvent.toJsonObject())
                    try {
                        val response = collectPublishResponse(eventId = nostrEvent.id)
                        responseFlow.emit(NostrPublishResult(result = response))
                    } catch (error: NostrNoticeException) {
                        responseFlow.emit(NostrPublishResult(error = error))
                    } catch (error: TimeoutCancellationException) {
                        responseFlow.emit(NostrPublishResult(error = error))
                    }
                }
            }
        }

        responseFlow.timeout(30.seconds)
            .catch { throw NostrPublishException(cause = it) }
            .first { it.isSuccessful() }
    }
}

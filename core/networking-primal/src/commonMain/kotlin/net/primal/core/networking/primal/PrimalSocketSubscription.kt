package net.primal.core.networking.primal

import io.github.aakira.napier.Napier
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.networking.sockets.toPrimalSubscriptionId

class PrimalSocketSubscription<T> private constructor(
    private val scope: CoroutineScope,
    private val primalApiClient: PrimalApiClient,
    private val cacheFilter: PrimalCacheFilter,
    private val transformer: (NostrIncomingMessage.EventMessage) -> T?,
    private val onEvent: suspend (T) -> Unit,
) {

    private var id: String? = null
    private var job: Job? = null

    companion object {
        fun <T> launch(
            scope: CoroutineScope,
            primalApiClient: PrimalApiClient,
            cacheFilter: PrimalCacheFilter,
            transformer: NostrIncomingMessage.EventMessage.() -> T?,
            onEvent: suspend (T) -> Unit,
        ): PrimalSocketSubscription<T> {
            return PrimalSocketSubscription(
                primalApiClient = primalApiClient,
                cacheFilter = cacheFilter,
                transformer = transformer,
                onEvent = onEvent,
                scope = scope,
            ).apply {
                this.job = this.scope.launch {
                    try {
                        subscribe()
                    } catch (error: WssException) {
                        Napier.w(error) { "Subscription failed." }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun subscribe() {
        val newSubscriptionId = Uuid.random().toPrimalSubscriptionId()
        this.id = newSubscriptionId
        primalApiClient.subscribe(
            subscriptionId = newSubscriptionId,
            message = cacheFilter,
        ).transform {
            when (it) {
                is NostrIncomingMessage.EventMessage -> transformer(it)?.let { emit(it) }
                else -> Unit
            }
        }.collect {
            onEvent(it)
        }
    }

    suspend fun unsubscribe() {
        this.id?.let {
            primalApiClient.closeSubscription(it)
            this.id = null
            this.job?.cancel()
            this.job = null
        }
    }
}

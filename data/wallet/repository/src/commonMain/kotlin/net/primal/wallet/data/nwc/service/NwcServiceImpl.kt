package net.primal.wallet.data.nwc.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.connections.nostr.NwcRepository
import net.primal.domain.connections.nostr.NwcService
import net.primal.wallet.data.nwc.manager.NwcBudgetManager

class NwcServiceImpl internal constructor(
    dispatchers: DispatcherProvider,
    private val nwcBudgetManager: NwcBudgetManager,
    private val nwcRepository: NwcRepository,
) : NwcService {

    private val scope = CoroutineScope(dispatchers.io() + SupervisorJob())

    override fun initialize(userId: String) {
        observeConnections(userId = userId)
    }

    override fun destroy() {
        scope.cancel()
    }

    private fun observeConnections(userId: String) =
        scope.launch {
            nwcRepository.observeConnections(userId = userId)
                .collect { connections ->
                    /* Start subscription to relay for all connection pubkeys */
                }
        }
}

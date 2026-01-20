package net.primal.wallet.data.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.connections.nostr.NostrWalletConnectionService
import net.primal.domain.connections.nostr.NwcRepository
import net.primal.wallet.data.manager.NwcBudgetManager

class NostrWalletConnectionServiceImpl internal constructor(
    dispatchers: DispatcherProvider,
    private val nwcBudgetManager: NwcBudgetManager,
    private val nwcRepository: NwcRepository,
) : NostrWalletConnectionService {

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
                    /* TODO: start subscription to relay for all connection pubkeys */
                }
        }
}

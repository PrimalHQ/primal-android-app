package net.primal.wallet.data.syncer.balance

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.WalletRepository
import net.primal.domain.wallet.sync.ActiveWalletBalanceSyncer

class ActiveWalletBalanceSyncerImpl(
    dispatcherProvider: DispatcherProvider,
    private val userId: String,
    private val walletAccountRepository: WalletAccountRepository,
    private val walletRepository: WalletRepository,
) : ActiveWalletBalanceSyncer {

    private val scope = CoroutineScope(dispatcherProvider.io() + SupervisorJob())

    private var activeWalletIdObserverJob: Job? = null
    private var walletSyncerJob: Job? = null

    private var activeWalletId: String? = null

    override fun getCurrentWalletId(): String? = activeWalletId

    override fun start() {
        activeWalletIdObserverJob?.cancel()
        activeWalletIdObserverJob = scope.launch {
            walletAccountRepository.observeActiveWalletId(userId)
                .collect { walletId ->
                    activeWalletId = walletId
                    walletSyncerJob?.cancel()
                    if (walletId != null) {
                        walletSyncerJob = scope.launch {
                            repeat(times = 10) {
                                runCatching {
                                    walletRepository.subscribeToWalletBalance(walletId = walletId).collect()
                                }
                                delay(5.seconds)
                            }
                        }
                    }
                }
        }
    }

    override fun stop() {
        walletSyncerJob?.cancel()
        activeWalletIdObserverJob?.cancel()
    }
}

package net.primal.android.wallet.init

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.wallet.data.repository.WalletSessionProvider

@Singleton
class WalletSessionBridge @Inject constructor(
    dispatchers: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val walletSessionProvider: WalletSessionProvider,
) {

    private val scope = CoroutineScope(dispatchers.io())

    fun start() {
        walletSessionProvider.start()
        scope.launch {
            activeAccountStore.activeUserId
                .map { it.takeIf { id -> id.isNotBlank() } }
                .distinctUntilChanged()
                .collect { walletSessionProvider.setActiveUserId(it) }
        }
    }
}

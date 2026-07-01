package net.primal.android.user.zaps

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.notifications.DEFAULT_ZAP_CONFIG
import net.primal.domain.notifications.DEFAULT_ZAP_DEFAULT
import net.primal.domain.utils.isConfigured
import net.primal.domain.zaps.ZappingState

@Singleton
class ZappingStateStore @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    activeAccountStore: ActiveAccountStore,
    private val walletAccountRepository: WalletAccountRepository,
) {

    private val scope = CoroutineScope(dispatcherProvider.io() + SupervisorJob())

    @OptIn(ExperimentalCoroutinesApi::class)
    val zappingState: StateFlow<ZappingState> =
        activeAccountStore.activeUserId
            .flatMapLatest { userId -> walletAccountRepository.observeActiveWallet(userId) }
            .combine(activeAccountStore.activeUserAccount) { userWallet, account ->
                val wallet = userWallet?.wallet
                ZappingState(
                    walletConnected = wallet.isConfigured(),
                    walletBalanceInBtc = wallet?.balanceInBtc?.formatAsString(),
                    zapDefault = account.appSettings?.zapDefault ?: DEFAULT_ZAP_DEFAULT,
                    zapsConfig = account.appSettings?.zapsConfig ?: DEFAULT_ZAP_CONFIG,
                )
            }
            .stateIn(scope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), ZappingState())
}

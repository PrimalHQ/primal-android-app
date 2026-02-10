package net.primal.android.settings.wallet.nwc.primal.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.service.PrimalNwcService
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.settings.wallet.nwc.primal.create.CreateNewWalletConnectionContract.UiEvent
import net.primal.android.settings.wallet.nwc.primal.create.CreateNewWalletConnectionContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.connections.nostr.NwcRepository
import net.primal.domain.connections.primal.PrimalWalletNwcRepository
import net.primal.domain.wallet.Wallet

@HiltViewModel
class CreateNewWalletConnectionViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val walletAccountRepository: WalletAccountRepository,
    private val primalWalletNwcRepository: PrimalWalletNwcRepository,
    private val nwcRepository: NwcRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(activeUserId = activeAccountStore.activeUserId()))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeActiveAccount()
        observeServiceRunningState()
        observeEvents()
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        activeUserId = it.pubkey,
                        activeAccountAvatarCdnImage = it.avatarCdnImage,
                        activeAccountLegendaryCustomization = it.primalLegendProfile?.asLegendaryCustomization(),
                        activeAccountBlossoms = it.blossomServers,
                        activeAccountDisplayName = it.authorDisplayName,
                    )
                }
            }
        }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeServiceRunningState() =
        viewModelScope.launch {
            activeAccountStore.activeUserId
                .flatMapLatest { userId ->
                    PrimalNwcService.isRunningForUser(userId)
                }
                .collect { isRunning ->
                    setState { copy(isServiceRunningForCurrentUser = isRunning) }
                }
        }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.AppNameChanged -> setState {
                        copy(appName = it.appName)
                    }

                    UiEvent.CreateWalletConnection -> {
                        if (state.value.appName.isNotEmpty()) {
                            createNewWalletConnection(
                                appName = state.value.appName,
                                dailyBudget = state.value.dailyBudget,
                            )
                        }
                    }

                    is UiEvent.DailyBudgetChanged -> setState {
                        copy(dailyBudget = it.dailyBudget)
                    }
                }
            }
        }
    }

    private fun createNewWalletConnection(appName: String, dailyBudget: Long?) =
        viewModelScope.launch {
            setState { copy(creatingSecret = true) }
            runCatching {
                val userId = activeAccountStore.activeUserId()
                when (val activeWallet = walletAccountRepository.getActiveWallet(userId)) {
                    is Wallet.Spark -> {
                        nwcRepository.createNewWalletConnection(
                            userId = userId,
                            walletId = activeWallet.walletId,
                            appName = appName,
                            dailyBudget = dailyBudget,
                        ).getOrThrow()
                    }
                    is Wallet.Primal -> {
                        val dailyBudgetBtc = dailyBudget?.toBtc()
                        val formattedDailyBudgetBtc = dailyBudgetBtc?.let {
                            BigDecimal(it.toString())
                                .stripTrailingZeros()
                                .toPlainString()
                        }
                        primalWalletNwcRepository.createNewWalletConnection(
                            userId = userId,
                            appName = appName,
                            dailyBudget = formattedDailyBudgetBtc,
                        ).nwcConnectionUri
                    }
                    else -> error("Active wallet does not support NWC connections.")
                }
            }.onSuccess { nwcConnectionUri ->
                setState {
                    copy(
                        nwcConnectionUri = nwcConnectionUri,
                        creatingSecret = false,
                    )
                }
            }.onFailure { error ->
                setState { copy(creatingSecret = false) }
                Napier.w(throwable = error) { "Failed to create wallet connection." }
            }
        }
}

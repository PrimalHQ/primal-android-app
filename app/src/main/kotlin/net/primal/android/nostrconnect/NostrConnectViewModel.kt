package net.primal.android.nostrconnect

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.drawer.multiaccount.model.asUserAccountUi
import net.primal.android.navigation.nostrConnectImageUrl
import net.primal.android.navigation.nostrConnectName
import net.primal.android.navigation.nostrConnectUrl
import net.primal.android.nostrconnect.NostrConnectContract.Companion.DAILY_BUDGET_OPTIONS
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.LoginType
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.repository.isValidExchangeRate
import net.primal.core.utils.CurrencyConversionUtils.fromSatsToUsd
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import timber.log.Timber

@HiltViewModel
class NostrConnectViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountsStore: UserAccountsStore,
    private val activeAccountStore: ActiveAccountStore,
    private val exchangeRateHandler: ExchangeRateHandler,
    private val credentialsStore: CredentialsStore,
) : ViewModel() {

    private val name = savedStateHandle.nostrConnectName
    private val url = savedStateHandle.nostrConnectUrl
    private val imageUrl = savedStateHandle.nostrConnectImageUrl

    private val _state = MutableStateFlow(
        NostrConnectContract.UiState(
            appName = name,
            appUrl = url,
            appImageUrl = imageUrl,
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: NostrConnectContract.UiState.() -> NostrConnectContract.UiState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<NostrConnectContract.UiEvent>()
    fun setEvent(event: NostrConnectContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<NostrConnectContract.SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: NostrConnectContract.SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
        observeAccounts()
        observeUsdExchangeRate()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is NostrConnectContract.UiEvent.TabChanged -> setState { copy(selectedTab = it.tab) }
                    is NostrConnectContract.UiEvent.AccountSelected ->
                        setState { copy(selectedAccount = accounts.find { acc -> acc.pubkey == it.pubkey }) }
                    is NostrConnectContract.UiEvent.TrustLevelSelected -> setState { copy(trustLevel = it.level) }
                    is NostrConnectContract.UiEvent.ConnectClicked -> connect()
                    is NostrConnectContract.UiEvent.DailyBudgetClicked -> setState {
                        copy(showDailyBudgetPicker = true, selectedDailyBudget = this.dailyBudget)
                    }
                    is NostrConnectContract.UiEvent.DailyBudgetChanged -> setState {
                        copy(
                            selectedDailyBudget = it.budget,
                        )
                    }
                    is NostrConnectContract.UiEvent.DailyBudgetApplied -> setState {
                        copy(dailyBudget = this.selectedDailyBudget, showDailyBudgetPicker = false)
                    }
                    is NostrConnectContract.UiEvent.DailyBudgetCancelled -> setState {
                        copy(
                            showDailyBudgetPicker = false,
                        )
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun observeAccounts() {
        viewModelScope.launch {
            accountsStore.userAccounts.collect { userAccounts ->
                val allCredentials = credentialsStore.credentials.value
                val nsecOnlyUserAccounts = userAccounts.filter { userAccount ->
                    val credential = allCredentials.find { credential ->
                        credential.npub == userAccount.pubkey.hexToNpubHrp()
                    }
                    credential?.type == LoginType.PrivateKey
                }

                val accounts = nsecOnlyUserAccounts.map { it.asUserAccountUi() }
                val activeAccount = activeAccountStore.activeUserAccount().asUserAccountUi()

                val selectedAccount = accounts.find { it.pubkey == activeAccount.pubkey }
                    ?: accounts.firstOrNull()

                setState {
                    copy(
                        accounts = accounts,
                        selectedAccount = selectedAccount,
                    )
                }
            }
        }
    }

    private fun observeUsdExchangeRate() {
        viewModelScope.launch {
            fetchExchangeRate()
            exchangeRateHandler.usdExchangeRate.collect { exchangeRate ->
                val budgetToUsdMap = calculateBudgetToUsdMap(exchangeRate)
                setState { copy(budgetToUsdMap = budgetToUsdMap) }
            }
        }
    }

    private fun fetchExchangeRate() =
        viewModelScope.launch {
            exchangeRateHandler.updateExchangeRate(
                userId = activeAccountStore.activeUserId(),
            )
        }

    private fun calculateBudgetToUsdMap(exchangeRate: Double?): Map<Long, BigDecimal?> {
        if (!exchangeRate.isValidExchangeRate()) {
            return emptyMap()
        }

        return DAILY_BUDGET_OPTIONS.associateWith { sats ->
            sats.toBigDecimal().fromSatsToUsd(exchangeRate)
        }
    }

    private fun connect() {
        viewModelScope.launch {
            setState { copy(connecting = true) }
            try {
                val selectedAccount = state.value.selectedAccount
                    ?: throw IllegalStateException("No account selected.")
                val connectionUrl = state.value.appUrl
                    ?: throw IllegalStateException("No connection URL found.")

                initialize(
                    signerPubKey = selectedAccount.pubkey,
                    userPubKey = selectedAccount.pubkey,
                    connectionUrl = connectionUrl,
                )
                setEffect(NostrConnectContract.SideEffect.ConnectionSuccess)
            } catch (e: Exception) {
                Timber.e(e)
                setEffect(NostrConnectContract.SideEffect.ConnectionFailed(e))
            } finally {
                setState { copy(connecting = false) }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private suspend fun initialize(
        signerPubKey: String,
        userPubKey: String,
        connectionUrl: String,
    ): Result<Unit> {
        val mockDelay = 1500L
        delay(mockDelay)
        return Result.success(Unit)
    }
}

package net.primal.android.nostrconnect

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.di.SignerConnectionInitializerFactory
import net.primal.android.core.errors.UiError
import net.primal.android.core.push.PushNotificationsTokenUpdater
import net.primal.android.drawer.multiaccount.model.asUserAccountUi
import net.primal.android.navigation.nostrConnectUri
import net.primal.android.nostrconnect.NostrConnectContract.Companion.DAILY_BUDGET_OPTIONS
import net.primal.android.nostrconnect.handler.RemoteSignerSessionHandler
import net.primal.android.nostrconnect.utils.getNostrConnectImage
import net.primal.android.nostrconnect.utils.getNostrConnectName
import net.primal.android.nostrconnect.utils.getNostrConnectUrl
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.CredentialType
import net.primal.android.user.domain.asKeyPair
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.repository.isValidExchangeRate
import net.primal.core.utils.CurrencyConversionUtils.fromSatsToUsd
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import timber.log.Timber

@HiltViewModel
class NostrConnectViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountsStore: UserAccountsStore,
    private val activeAccountStore: ActiveAccountStore,
    private val exchangeRateHandler: ExchangeRateHandler,
    private val credentialsStore: CredentialsStore,
    private val signerConnectionInitializerFactory: SignerConnectionInitializerFactory,
    private val signerSessionHandler: RemoteSignerSessionHandler,
    private val tokenUpdater: PushNotificationsTokenUpdater,
) : ViewModel() {

    private val connectionUrl = savedStateHandle.nostrConnectUri

    private val _state = MutableStateFlow(
        NostrConnectContract.UiState(
            appName = connectionUrl?.getNostrConnectName(),
            appWebUrl = connectionUrl?.getNostrConnectUrl(),
            appImageUrl = connectionUrl?.getNostrConnectImage(),
            connectionUrl = connectionUrl,
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
                    is NostrConnectContract.UiEvent.ChangeTab -> setState { copy(selectedTab = it.tab) }
                    is NostrConnectContract.UiEvent.SelectAccount ->
                        setState { copy(selectedAccount = accounts.find { acc -> acc.pubkey == it.pubkey }) }

                    is NostrConnectContract.UiEvent.SelectTrustLevel -> setState { copy(trustLevel = it.level) }
                    is NostrConnectContract.UiEvent.ClickConnect -> connect()
                    is NostrConnectContract.UiEvent.ClickDailyBudget -> setState {
                        copy(showDailyBudgetPicker = true, selectedDailyBudget = this.dailyBudget)
                    }

                    is NostrConnectContract.UiEvent.ChangeDailyBudget -> setState {
                        copy(
                            selectedDailyBudget = it.budget,
                        )
                    }

                    is NostrConnectContract.UiEvent.ApplyDailyBudget -> setState {
                        copy(dailyBudget = this.selectedDailyBudget, showDailyBudgetPicker = false)
                    }

                    is NostrConnectContract.UiEvent.CancelDailyBudget -> setState {
                        copy(
                            showDailyBudgetPicker = false,
                        )
                    }

                    NostrConnectContract.UiEvent.DismissError -> setState { copy(error = null) }
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
                    credential?.type == CredentialType.PrivateKey
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
            val selectedAccount = state.value.selectedAccount ?: return@launch
            val connectionUrl = state.value.connectionUrl ?: return@launch

            val signerKeyPair = credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair()
            val initializer = signerConnectionInitializerFactory.create(signerKeyPair = signerKeyPair)

            initializer.initialize(
                signerPubKey = signerKeyPair.pubKey,
                userPubKey = selectedAccount.pubkey,
                connectionUrl = connectionUrl,
                trustLevel = state.value.trustLevel,
            ).onSuccess {
                runCatching { tokenUpdater.updateTokenForRemoteSigner() }
                signerSessionHandler.startSession(connectionId = it.connectionId)
                setEffect(NostrConnectContract.SideEffect.ConnectionSuccess)
            }.onFailure { error ->
                Timber.e(error)
                setState { copy(error = UiError.GenericError()) }
            }

            setState { copy(connecting = false) }
        }
    }
}

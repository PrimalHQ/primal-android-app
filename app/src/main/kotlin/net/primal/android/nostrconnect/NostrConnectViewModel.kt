package net.primal.android.nostrconnect

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.compose.signer.SignerConnectBottomSheet.DAILY_BUDGET_OPTIONS
import net.primal.android.core.errors.UiError
import net.primal.android.core.push.PushNotificationsTokenUpdater
import net.primal.android.drawer.multiaccount.model.asUserAccountUi
import net.primal.android.navigation.nostrConnectUri
import net.primal.android.nostrconnect.utils.getNostrConnectCallback
import net.primal.android.nostrconnect.utils.getNostrConnectImage
import net.primal.android.nostrconnect.utils.getNostrConnectName
import net.primal.android.nostrconnect.utils.getNostrConnectUrl
import net.primal.android.nostrconnect.utils.hasNwcOption
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.CredentialType
import net.primal.android.user.domain.asKeyPair
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.repository.isValidExchangeRate
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.core.utils.CurrencyConversionUtils.fromSatsToUsd
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.data.account.repository.repository.SignerConnectionInitializer
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.connections.PrimalWalletNwcRepository
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import timber.log.Timber

@HiltViewModel
class NostrConnectViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    private val accountsStore: UserAccountsStore,
    private val activeAccountStore: ActiveAccountStore,
    private val exchangeRateHandler: ExchangeRateHandler,
    private val credentialsStore: CredentialsStore,
    private val signerConnectionInitializer: SignerConnectionInitializer,
    private val primalWalletNwcRepository: PrimalWalletNwcRepository,
    private val tokenUpdater: PushNotificationsTokenUpdater,
) : ViewModel() {

    private val connectionUrl = savedStateHandle.nostrConnectUri

    private val _state = MutableStateFlow(
        NostrConnectContract.UiState(
            appName = connectionUrl?.getNostrConnectName(),
            appDescription = connectionUrl?.getNostrConnectUrl(),
            appImageUrl = connectionUrl?.getNostrConnectImage(),
            connectionUrl = connectionUrl,
            callback = connectionUrl?.getNostrConnectCallback(),
            hasNwcRequest = connectionUrl?.hasNwcOption() == true,
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
        if (connectionUrl?.hasNwcOption() == true) {
            fetchExchangeRate()
            observeUsdExchangeRate()
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is NostrConnectContract.UiEvent.ConnectUser -> connect(
                        userId = it.userId,
                        trustLevel = it.trustLevel,
                        dailyBudget = it.dailyBudget,
                    )

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

                val accounts = nsecOnlyUserAccounts
                    .sortedByDescending { it.lastAccessedAt }
                    .map { it.asUserAccountUi() }

                setState { copy(accounts = accounts) }
            }
        }
    }

    private fun fetchExchangeRate() =
        viewModelScope.launch {
            exchangeRateHandler.updateExchangeRate(
                userId = activeAccountStore.activeUserId(),
            )
        }

    private fun observeUsdExchangeRate() {
        viewModelScope.launch {
            exchangeRateHandler.usdExchangeRate.collect { exchangeRate ->
                val budgetToUsdMap = calculateBudgetToUsdMap(exchangeRate)
                setState { copy(budgetToUsdMap = budgetToUsdMap) }
            }
        }
    }

    private fun calculateBudgetToUsdMap(exchangeRate: Double?): Map<Long, BigDecimal?> {
        if (!exchangeRate.isValidExchangeRate()) {
            return emptyMap()
        }

        return DAILY_BUDGET_OPTIONS.associateWith { sats ->
            sats.toString().toBigDecimal().fromSatsToUsd(exchangeRate)
        }
    }

    private fun connect(
        userId: String,
        trustLevel: TrustLevel,
        dailyBudget: Long?,
    ) {
        viewModelScope.launch {
            setState { copy(connecting = true) }
            val currentState = state.value
            val connectionUrl = currentState.connectionUrl ?: return@launch

            var nwcConnectionString: String? = null
            if (currentState.hasNwcRequest && dailyBudget != 0L) {
                val budgetBtc = dailyBudget?.toBtc()?.formatAsString()
                runCatching {
                    primalWalletNwcRepository.createNewWalletConnection(
                        userId = userId,
                        appName = currentState.appName ?: "External App",
                        dailyBudget = budgetBtc,
                    )
                }.onSuccess { nwcConnection ->
                    nwcConnectionString = nwcConnection.nwcConnectionUri
                }.onFailure { error ->
                    Timber.e(error)
                }
            }

            val signerKeyPair = credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair()

            signerConnectionInitializer.initialize(
                signerPubKey = signerKeyPair.pubKey,
                userPubKey = userId,
                connectionUrl = connectionUrl,
                trustLevel = trustLevel,
                nwcConnectionString = nwcConnectionString,
            ).onSuccess {
                CoroutineScope(dispatcherProvider.io()).launch {
                    runCatching { tokenUpdater.updateTokenForRemoteSigner() }
                }
                setEffect(
                    NostrConnectContract.SideEffect.ConnectionSuccess(
                        callbackUri = state.value.callback,
                    ),
                )
            }.onFailure { error ->
                Timber.e(error)
                setState { copy(error = UiError.GenericError()) }
            }

            setState { copy(connecting = false) }
        }
    }
}

package net.primal.android.nostrconnect

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import net.primal.android.core.errors.UiError
import net.primal.android.core.push.PushNotificationsTokenUpdater
import net.primal.android.drawer.multiaccount.model.asUserAccountUi
import net.primal.android.navigation.nostrConnectUri
import net.primal.android.nostrconnect.utils.getNostrConnectImage
import net.primal.android.nostrconnect.utils.getNostrConnectName
import net.primal.android.nostrconnect.utils.getNostrConnectUrl
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.CredentialType
import net.primal.android.user.domain.asKeyPair
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.data.account.repository.repository.SignerConnectionInitializer
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import timber.log.Timber

@HiltViewModel
class NostrConnectViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    private val accountsStore: UserAccountsStore,
    // private val exchangeRateHandler: ExchangeRateHandler,
    private val credentialsStore: CredentialsStore,
    private val signerConnectionInitializer: SignerConnectionInitializer,
    private val tokenUpdater: PushNotificationsTokenUpdater,
) : ViewModel() {

    private val connectionUrl = savedStateHandle.nostrConnectUri

    private val _state = MutableStateFlow(
        NostrConnectContract.UiState(
            appName = connectionUrl?.getNostrConnectName(),
            appDescription = connectionUrl?.getNostrConnectUrl(),
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
        // observeUsdExchangeRate()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is NostrConnectContract.UiEvent.ConnectUser -> connect(it.userId, it.trustLevel)
                    /*
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
                     */
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

    /*
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
     */

    private fun connect(userId: String, trustLevel: TrustLevel) {
        viewModelScope.launch {
            setState { copy(connecting = true) }
            val connectionUrl = state.value.connectionUrl ?: return@launch

            val signerKeyPair = credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair()

            signerConnectionInitializer.initialize(
                signerPubKey = signerKeyPair.pubKey,
                userPubKey = userId,
                connectionUrl = connectionUrl,
                trustLevel = trustLevel,
            ).onSuccess {
                CoroutineScope(dispatcherProvider.io()).launch {
                    runCatching { tokenUpdater.updateTokenForRemoteSigner() }
                }
                setEffect(NostrConnectContract.SideEffect.ConnectionSuccess)
            }.onFailure { error ->
                Timber.e(error)
                setState { copy(error = UiError.GenericError()) }
            }

            setState { copy(connecting = false) }
        }
    }
}

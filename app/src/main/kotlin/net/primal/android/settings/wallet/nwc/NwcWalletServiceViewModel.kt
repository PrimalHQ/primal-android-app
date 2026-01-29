package net.primal.android.settings.wallet.nwc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.settings.wallet.nwc.NwcWalletServiceContract.UiEvent
import net.primal.android.settings.wallet.nwc.NwcWalletServiceContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.connections.nostr.NwcRepository

@HiltViewModel
class NwcWalletServiceViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val walletAccountRepository: WalletAccountRepository,
    private val nwcRepository: NwcRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        loadWalletId()
    }

    private fun loadWalletId() {
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            val wallet = walletAccountRepository.getActiveWallet(userId)
            setState { copy(walletId = wallet?.walletId) }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    UiEvent.CreateConnection -> createNwcConnection()
                    UiEvent.DismissError -> setState { copy(error = null) }
                    is UiEvent.ChangeDailyBudget -> setState { copy(dailyBudgetInput = event.value) }
                }
            }
        }
    }

    private fun createNwcConnection() {
        val walletId = state.value.walletId
        if (walletId == null) {
            setState { copy(error = "No active wallet found") }
            return
        }

        viewModelScope.launch {
            setState { copy(isCreating = true, error = null) }

            val userId = activeAccountStore.activeUserId()
            val dailyBudget = state.value.dailyBudgetInput.toLongOrNull()

            runCatching {
                nwcRepository.createNewWalletConnection(
                    userId = userId,
                    walletId = walletId,
                    appName = "NWC Test Connection",
                    dailyBudget = dailyBudget,
                ).getOrThrow()
            }
                .onSuccess { nwcUri ->
                    Napier.d { "NWC Connection created: $nwcUri" }
                    setState { copy(nwcConnectionUri = nwcUri, isCreating = false) }
                }
                .onFailure { throwable ->
                    Napier.e(throwable) { "Failed to create NWC connection" }
                    setState { copy(error = throwable.message, isCreating = false) }
                }
        }
    }
}

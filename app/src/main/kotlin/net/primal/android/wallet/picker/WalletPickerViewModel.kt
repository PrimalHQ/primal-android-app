package net.primal.android.wallet.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.picker.WalletPickerContract.UiEvent
import net.primal.android.wallet.picker.WalletPickerContract.UiState
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.Wallet

@HiltViewModel
class WalletPickerViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val walletAccountRepository: WalletAccountRepository,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
    private val primalWalletAccountRepository: PrimalWalletAccountRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeWallets()
        observeActiveWalletId()
        fetchRegisteredWallet()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.ChangeActiveWallet -> changeActiveWallet(event.userWallet.wallet)
                    is UiEvent.EnterEditMode -> setState {
                        copy(isEditMode = true, previewRegisteredWalletId = registeredWalletId)
                    }

                    is UiEvent.CancelEditMode -> setState {
                        copy(isEditMode = false, previewRegisteredWalletId = null)
                    }

                    is UiEvent.SelectWalletForReassignment -> setState {
                        copy(previewRegisteredWalletId = event.userWallet.wallet.walletId)
                    }

                    is UiEvent.ConfirmReassignment -> confirmReassignment()
                    is UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun observeWallets() =
        viewModelScope.launch {
            walletAccountRepository.observeWalletsByUser(activeAccountStore.activeUserId())
                .distinctUntilChanged()
                .collect { wallets ->
                    setState { copy(wallets = wallets) }
                }
        }

    private fun observeActiveWalletId() =
        viewModelScope.launch {
            walletAccountRepository.observeActiveWalletId(activeAccountStore.activeUserId())
                .distinctUntilChanged()
                .collect { walletId ->
                    setState { copy(activeWalletId = walletId) }
                }
        }

    private fun fetchRegisteredWallet() =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            primalWalletAccountRepository.fetchWalletStatus(userId)
                .onSuccess { status ->
                    val registeredId = status.registeredSparkWalletId
                        ?: if (status.hasCustodialWallet) userId else null
                    setState {
                        copy(
                            registeredWalletId = registeredId,
                            registeredLightningAddress = status.lightningAddress,
                        )
                    }
                }
                .onFailure { error ->
                    Napier.w(throwable = error) { "Failed to fetch wallet status for registered wallet" }
                }
        }

    private fun changeActiveWallet(wallet: Wallet) =
        viewModelScope.launch {
            walletAccountRepository.setActiveWallet(
                userId = activeAccountStore.activeUserId(),
                walletId = wallet.walletId,
            )
        }

    private fun confirmReassignment() =
        viewModelScope.launch {
            val currentState = state.value
            val previewWalletId = currentState.previewRegisteredWalletId

            if (previewWalletId == null || previewWalletId == currentState.registeredWalletId) {
                setState { copy(isEditMode = false, previewRegisteredWalletId = null) }
                return@launch
            }

            val targetWallet = currentState.wallets.find { it.wallet.walletId == previewWalletId }?.wallet
                ?: return@launch

            setState { copy(isReassigning = true) }

            val userId = activeAccountStore.activeUserId()
            val reassignResult = when (targetWallet) {
                is Wallet.Spark -> sparkWalletAccountRepository.registerSparkWallet(userId, targetWallet.walletId)
                is Wallet.Primal -> {
                    val sparkWalletId = currentState.registeredWalletId
                    if (sparkWalletId == null) {
                        setState { copy(isReassigning = false) }
                        return@launch
                    }
                    sparkWalletAccountRepository.unregisterSparkWallet(userId, sparkWalletId)
                }

                is Wallet.NWC -> {
                    setState { copy(isReassigning = false) }
                    return@launch
                }
            }

            reassignResult.onFailure { error ->
                Napier.e(throwable = error) { "Failed to reassign Lightning address" }
                setState { copy(isReassigning = false, error = error) }
                return@launch
            }

            val statusResult = primalWalletAccountRepository.fetchWalletStatus(userId)
            val registeredId = statusResult.getOrNull()?.let { status ->
                status.registeredSparkWalletId ?: if (status.hasCustodialWallet) userId else null
            } ?: targetWallet.walletId

            setState {
                copy(
                    registeredWalletId = registeredId,
                    registeredLightningAddress = statusResult.getOrNull()?.lightningAddress
                        ?: registeredLightningAddress,
                    isEditMode = false,
                    previewRegisteredWalletId = null,
                    isReassigning = false,
                )
            }
        }
}

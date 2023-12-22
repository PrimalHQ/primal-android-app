package net.primal.android.wallet.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.WalletPreference
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.activation.WalletActivationContract.UiEvent
import net.primal.android.wallet.activation.WalletActivationContract.UiState
import net.primal.android.wallet.repository.WalletRepository
import timber.log.Timber

@HiltViewModel
class WalletActivationViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(value = UiState())
    val uiState = _uiState.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _uiState.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        subscribeToEvents()
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.ActivationDataChanged -> setState {
                        copy(
                            name = it.name,
                            email = it.email,
                            error = null,
                        )
                    }

                    is UiEvent.Activate -> onActivateWallet(code = it.code)
                    is UiEvent.ActivationRequest -> onActivationRequest(name = it.name, email = it.email)
                    UiEvent.ClearErrorMessage -> setState { copy(error = null) }
                    UiEvent.RequestBackToDataInput -> setState { copy(status = WalletActivationStatus.PendingData) }
                }
            }
        }

    private fun onActivationRequest(name: String, email: String) =
        viewModelScope.launch {
            setState { copy(working = true) }
            try {
                val userId = activeAccountStore.activeUserId()
                walletRepository.requestActivationCodeToEmail(userId, name, email)
                setState { copy(status = WalletActivationStatus.PendingCodeConfirmation) }
            } catch (error: WssException) {
                Timber.e(error)
                setState { copy(error = error) }
            } finally {
                setState { copy(working = false) }
            }
        }

    private fun onActivateWallet(code: String) =
        viewModelScope.launch {
            setState { copy(working = true) }
            try {
                val userId = activeAccountStore.activeUserId()
                val lightningAddress = walletRepository.activateWallet(userId, code)
                walletRepository.fetchUserWalletInfoAndUpdateUserAccount(userId)

                val activeUser = activeAccountStore.activeUserAccount()
                if (activeUser.nostrWallet == null) {
                    walletRepository.updateWalletPreference(userId, WalletPreference.PrimalWallet)
                }
                activeUser.primalWallet?.lightningAddress?.let {
                    userRepository.setLightningAddress(userId = userId, lightningAddress = it)
                }

                setState {
                    copy(
                        activatedLightningAddress = lightningAddress,
                        status = WalletActivationStatus.ActivationSuccess,
                    )
                }
            } catch (error: WssException) {
                Timber.e(error)
                setState { copy(error = error) }
            } finally {
                setState { copy(working = false) }
            }
        }
}

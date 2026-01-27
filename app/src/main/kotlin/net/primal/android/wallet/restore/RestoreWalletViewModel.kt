package net.primal.android.wallet.restore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.wallet.restore.RestoreWalletContract.RestoreStage
import net.primal.android.wallet.restore.RestoreWalletContract.SideEffect
import net.primal.android.wallet.restore.RestoreWalletContract.UiEvent
import net.primal.android.wallet.restore.RestoreWalletContract.UiState
import net.primal.android.wallet.restore.RestoreWalletContract.UiState.MnemonicValidation
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.core.utils.runCatching
import net.primal.wallet.data.validator.RecoveryPhraseValidator

@HiltViewModel
class RestoreWalletViewModel @Inject constructor() : ViewModel() {

    private val recoveryPhraseValidator = RecoveryPhraseValidator()

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val _effect = Channel<SideEffect>()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.MnemonicChange -> handleMnemonicChange(it.mnemonic)
                    UiEvent.ValidateMnemonic -> validateMnemonic()
                    UiEvent.RestoreWalletClick -> restoreWallet()
                }
            }
        }

    private fun handleMnemonicChange(mnemonic: String) {
        setState {
            copy(
                mnemonic = mnemonic,
                mnemonicValidation = MnemonicValidation.Empty,
            )
        }
    }

    private fun validateMnemonic() {
        val mnemonic = state.value.mnemonic
        if (mnemonic.isBlank()) {
            setState { copy(mnemonicValidation = MnemonicValidation.Empty) }
            return
        }

        val isValid = recoveryPhraseValidator.isValid(mnemonic)

        setState {
            copy(
                mnemonicValidation = if (isValid) {
                    MnemonicValidation.Valid
                } else {
                    MnemonicValidation.Invalid
                },
            )
        }
    }

    private fun restoreWallet() =
        viewModelScope.launch {
            setState { copy(currentStage = RestoreStage.Restoring) }
            runCatching {
                delay(RESTORE_SIMULATION_DELAY_MILLIS)

                // Implement wallet restore logic
                if (state.value.mnemonic.contains("invalid")) {
                    error("Simulated restore error.")
                }
            }.onSuccess {
                setEffect(SideEffect.RestoreSuccess)
            }.onFailure { error ->
                Napier.e(throwable = error) { "Error restoring wallet." }
                setState {
                    copy(
                        currentStage = RestoreStage.MnemonicInput,
                        mnemonicValidation = MnemonicValidation.Invalid,
                    )
                }
            }
        }

    companion object {
        private const val RESTORE_SIMULATION_DELAY_MILLIS = 3000L
    }
}

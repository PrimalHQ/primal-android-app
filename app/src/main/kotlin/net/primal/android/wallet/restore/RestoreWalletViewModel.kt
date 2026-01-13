package net.primal.android.wallet.restore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import net.primal.android.wallet.restore.RestoreWalletContract.RestoreStage
import net.primal.android.wallet.restore.RestoreWalletContract.SideEffect
import net.primal.android.wallet.restore.RestoreWalletContract.UiEvent
import net.primal.android.wallet.restore.RestoreWalletContract.UiState
import net.primal.android.wallet.restore.RestoreWalletContract.UiState.MnemonicValidation
import timber.log.Timber

@HiltViewModel
class RestoreWalletViewModel @Inject constructor() : ViewModel() {

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
                    is UiEvent.MnemonicChanged -> handleMnemonicChange(it.mnemonic)
                    UiEvent.RestoreWalletClick -> restoreWallet()
                }
            }
        }

    private fun handleMnemonicChange(mnemonic: String) {
        setState {
            copy(
                mnemonic = mnemonic,
                mnemonicValidation = validateMnemonic(mnemonic),
                error = null,
            )
        }
    }

    private fun validateMnemonic(mnemonic: String): MnemonicValidation {
        if (mnemonic.isBlank()) return MnemonicValidation.Empty

        val wordCount = mnemonic.trim().split(Regex("\\s+")).count()

        return if (wordCount in MIN_RECOVERY_PHRASE_WORD_COUNT..MAX_RECOVERY_PHRASE_WORD_COUNT) {
            MnemonicValidation.Valid
        } else {
            MnemonicValidation.Empty
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun restoreWallet() =
        viewModelScope.launch {
            setState { copy(currentStage = RestoreStage.Restoring) }
            try {
                delay(RESTORE_SIMULATION_DELAY_MILLIS)

                // Implement wallet restore logic
                if (state.value.mnemonic.contains("invalid")) {
                    error("Simulated restore error.")
                }

                setEffect(SideEffect.RestoreSuccess)
            } catch (error: Exception) {
                Timber.e(error, "Error restoring wallet.")
                setState {
                    copy(
                        currentStage = RestoreStage.MnemonicInput,
                        error = "Invalid recovery phrase",
                    )
                }
            }
        }

    companion object {
        private const val MIN_RECOVERY_PHRASE_WORD_COUNT = 12
        private const val MAX_RECOVERY_PHRASE_WORD_COUNT = 24
        private const val RESTORE_SIMULATION_DELAY_MILLIS = 3000L
    }
}

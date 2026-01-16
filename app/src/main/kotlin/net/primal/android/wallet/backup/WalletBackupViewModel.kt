package net.primal.android.wallet.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.wallet.backup.WalletBackupContract.SideEffect
import net.primal.android.wallet.backup.WalletBackupContract.UiEvent
import net.primal.android.wallet.backup.WalletBackupContract.UiState

@HiltViewModel
class WalletBackupViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        subscribeToEvents()
        generateDummyData()
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    UiEvent.ProceedToPhraseDisplay -> setState {
                        copy(
                            currentStep = WalletBackupContract.BackupStep.SeedPhrase,
                        )
                    }
                    UiEvent.ProceedToVerification -> {
                        if (_state.value.seedPhrase.isNotEmpty()) {
                            val indices = (0 until _state.value.seedPhrase.size)
                                .shuffled()
                                .take(VERIFICATION_WORDS_COUNT)
                                .sorted()
                            setState {
                                copy(
                                    currentStep = WalletBackupContract.BackupStep.Verify,
                                    verificationIndices = indices,
                                )
                            }
                        }
                    }
                    UiEvent.ProceedToConfirmation -> setState {
                        copy(
                            currentStep = WalletBackupContract.BackupStep.Confirm,
                        )
                    }

                    UiEvent.RequestPreviousStep -> {
                        val currentStep = _state.value.currentStep
                        val previousStep = when (currentStep) {
                            WalletBackupContract.BackupStep.Verify -> WalletBackupContract.BackupStep.SeedPhrase
                            WalletBackupContract.BackupStep.SeedPhrase -> WalletBackupContract.BackupStep.Welcome
                            else -> null
                        }
                        if (previousStep != null) {
                            setState { copy(currentStep = previousStep) }
                        }
                    }

                    UiEvent.CompleteBackup -> {
                        setEffect(SideEffect.BackupCompleted)
                    }
                }
            }
        }

    private fun generateDummyData() {
        setState { copy(seedPhrase = DUMMY_WORDS) }
    }

    companion object {
        private const val VERIFICATION_WORDS_COUNT = 3
        private val DUMMY_WORDS = listOf(
            "embrace", "open", "universe", "acquire", "entire", "section",
            "direct", "loud", "acoustic", "erupt", "brave", "champion",
        )
    }
}

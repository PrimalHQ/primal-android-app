package net.primal.android.wallet.backup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.navigation.walletIdOrThrow
import net.primal.android.wallet.backup.WalletBackupContract.SideEffect
import net.primal.android.wallet.backup.WalletBackupContract.UiEvent
import net.primal.android.wallet.backup.WalletBackupContract.UiState
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.account.SparkWalletAccountRepository

@HiltViewModel
class WalletBackupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
) : ViewModel() {

    private val walletId: String = savedStateHandle.walletIdOrThrow

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
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    UiEvent.ProceedToPhraseDisplay -> fetchSeedPhraseAndProceed()

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

                    UiEvent.ProceedToConfirmation -> {
                        setState { copy(currentStep = WalletBackupContract.BackupStep.Confirm) }
                        launch {
                            delay(500.milliseconds)
                            setState { copy(seedPhrase = emptyList()) }
                        }
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

                    UiEvent.CompleteBackup -> markWalletAsBackedUp()
                }
            }
        }

    private fun fetchSeedPhraseAndProceed() {
        viewModelScope.launch {
            sparkWalletAccountRepository.getPersistedSeedWords(walletId)
                .onSuccess { seedWords ->
                    setState {
                        copy(
                            seedPhrase = seedWords,
                            currentStep = WalletBackupContract.BackupStep.SeedPhrase,
                        )
                    }
                }
                .onFailure { Napier.e(it) { "Seed phrase not available." } }
        }
    }

    private fun markWalletAsBackedUp() {
        viewModelScope.launch {
            sparkWalletAccountRepository.markWalletAsBackedUp(walletId)
                .onSuccess { setEffect(SideEffect.BackupCompleted) }
                .onFailure { Napier.e(it) { "Failed to mark wallet as backed up." } }
        }
    }

    companion object {
        private const val VERIFICATION_WORDS_COUNT = 3
    }
}

package net.primal.android.wallet.upgrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.upgrade.UpgradeWalletContract.UiEvent
import net.primal.android.wallet.upgrade.UpgradeWalletContract.UiState
import net.primal.domain.wallet.migration.MigrationProgress
import net.primal.domain.wallet.migration.MigrationStep
import net.primal.wallet.data.repository.handler.MigratePrimalToSparkWalletHandler

@HiltViewModel
class UpgradeWalletViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val migratePrimalToSparkWalletHandler: MigratePrimalToSparkWalletHandler,
) : ViewModel() {

    private val simulateUpgrade = false

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    UiEvent.StartUpgrade -> if (simulateUpgrade) simulateUpgradeFlow() else startUpgrade()
                    UiEvent.RetryUpgrade -> if (simulateUpgrade) simulateUpgradeFlow() else startUpgrade()
                }
            }
        }

    @Suppress("MagicNumber")
    private fun simulateUpgradeFlow() {
        viewModelScope.launch {
            setState { copy(status = UpgradeWalletStatus.Upgrading, currentStep = null, error = null) }
            delay(500)

            for (step in MigrationStep.entries) {
                setState { copy(currentStep = step) }
                delay(1500)
            }

            // Simulate success - change to test failure
//            setState { copy(status = UpgradeWalletStatus.Success, currentStep = null) }

//             Uncomment to test failure state:
            setState {
                copy(
                    status = UpgradeWalletStatus.Failed,
                    currentStep = MigrationStep.TRANSFERRING_FUNDS,
                    error = Exception("Simulated error: Connection timeout"),
                    errorLogs = listOf(
                        "[2024-01-15T10:30:00Z] I: Starting Primal→Spark migration for user abc123",
                        "[2024-01-15T10:30:01Z] D: Step: Creating Spark wallet locally",
                        "[2024-01-15T10:30:02Z] D: Step: Registering Spark wallet",
                        "[2024-01-15T10:30:03Z] D: Step: Checking Primal balance",
                        "[2024-01-15T10:30:04Z] D: Primal balance: 0.00015000",
                        "[2024-01-15T10:30:05Z] D: Step: Creating invoice on Spark wallet for 0.00015000 BTC",
                        "[2024-01-15T10:30:06Z] D: Step: Transferring funds from Primal to Spark",
                        "[2024-01-15T10:30:07Z] W: Withdraw failed (attempt 0, retry in 1s): Connection timeout",
                        "[2024-01-15T10:30:09Z] W: Withdraw failed (attempt 1, retry in 2s): Connection timeout",
                        "[2024-01-15T10:30:12Z] W: Withdraw failed (attempt 2, retry in 4s): Connection timeout",
                        "[2024-01-15T10:30:16Z] E: Migration failed at step TRANSFERRING_FUNDS: Connection timeout",
                    ),
                )
            }
        }
    }

    private fun startUpgrade() {
        viewModelScope.launch {
            setState { copy(status = UpgradeWalletStatus.Upgrading, currentStep = null, error = null) }

            migratePrimalToSparkWalletHandler.invoke(
                userId = activeAccountStore.activeUserId(),
                onProgress = { progress ->
                    when (progress) {
                        is MigrationProgress.NotStarted -> {
                            // Initializing migration
                        }

                        is MigrationProgress.InProgress -> {
                            Napier.i { "In progress at step ${progress.step}" }
                            setState { copy(currentStep = progress.step) }
                        }

                        is MigrationProgress.Completed -> {
                            setState { copy(status = UpgradeWalletStatus.Success, currentStep = null) }
                        }

                        is MigrationProgress.Failed -> {
                            Napier.w(throwable = progress.error) {
                                "Wallet migration failed at step ${progress.step}"
                            }
                            setState {
                                copy(
                                    status = UpgradeWalletStatus.Failed,
                                    currentStep = progress.step,
                                    error = progress.error,
                                    errorLogs = progress.logs,
                                )
                            }
                        }
                    }
                },
            )
        }
    }
}

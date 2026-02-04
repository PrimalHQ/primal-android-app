package net.primal.android.wallet.upgrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.upgrade.UpgradeWalletContract.UiState
import net.primal.domain.wallet.migration.MigrationProgress
import net.primal.wallet.data.repository.handler.MigratePrimalToSparkWalletHandler

@HiltViewModel
class UpgradeWalletViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val migratePrimalToSparkWalletHandler: MigratePrimalToSparkWalletHandler,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        startUpgrade()
    }

    private fun startUpgrade() {
        viewModelScope.launch {
            setState { copy(status = UpgradeWalletStatus.Upgrading, error = null) }

            migratePrimalToSparkWalletHandler.invoke(
                userId = activeAccountStore.activeUserId(),
                onProgress = { progress ->
                    when (progress) {
                        is MigrationProgress.NotStarted -> {
                            // Initializing migration
                        }

                        is MigrationProgress.InProgress -> {
                            // Keep upgrading status
                            Napier.i { "In progress at step ${progress.step}" }
                        }

                        is MigrationProgress.Completed -> {
                            setState { copy(status = UpgradeWalletStatus.Success) }
                        }

                        is MigrationProgress.Failed -> {
                            Napier.w(throwable = progress.error) {
                                "Wallet migration failed at step ${progress.step}"
                            }
                            setState { copy(status = UpgradeWalletStatus.Failed, error = progress.error) }
                        }
                    }
                },
            )
        }
    }
}

package net.primal.android.wallet.upgrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.wallet.upgrade.UpgradeWalletContract.UiState
import timber.log.Timber

@HiltViewModel
class UpgradeWalletViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        startUpgrade()
    }

    private fun startUpgrade() {
        viewModelScope.launch {
            runCatching {
                setState { copy(status = UpgradeWalletStatus.Upgrading, error = null) }
                delay(MOCK_UPGRADE_DELAY_MS)
            }.onFailure {
                Timber.w(it, "Wallet upgrade failed")
                setState { copy(status = UpgradeWalletStatus.Failed, error = it) }
            }.onSuccess {
                setState { copy(status = UpgradeWalletStatus.Success) }
            }
        }
    }

    companion object {
        private const val MOCK_UPGRADE_DELAY_MS = 2000L
    }
}

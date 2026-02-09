package net.primal.android.wallet.upgrade.sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.upgrade.sheet.UpgradeWalletSheetContract.UiEvent
import net.primal.android.wallet.upgrade.sheet.UpgradeWalletSheetContract.UiState
import net.primal.core.utils.onSuccess
import net.primal.domain.account.PrimalWalletAccountRepository

@HiltViewModel
class UpgradeWalletSheetViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val primalWalletAccountRepository: PrimalWalletAccountRepository,
) : ViewModel() {

    companion object {
        private val NOTICE_INTERVAL_MILLIS = 5.minutes.inWholeMilliseconds
    }

    private var lastNoticeShownAtMillis: Long? = null

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeActiveAccount()
        observeActiveAccountId()
        observeEvents()
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect { userAccount ->
                setState {
                    copy(
                        activeUserCdnImage = userAccount.avatarCdnImage,
                        activeUserLegendaryCustomization = userAccount.primalLegendProfile?.asLegendaryCustomization(),
                    )
                }
            }
        }

    private fun observeActiveAccountId() =
        viewModelScope.launch {
            activeAccountStore.activeUserId.collect { userId ->
                primalWalletAccountRepository.fetchWalletStatus(userId = userId)
                    .onSuccess {
                        val shouldUpgrade =
                            userId.isNotEmpty() &&
                                it.hasCustodialWallet &&
                                !it.hasMigratedToSparkWallet &&
                                !it.primalWalletDeprecated

                        setState { copy(shouldUserUpgrade = shouldUpgrade) }

                        if (shouldUpgrade) {
                            showNoticeIfIntervalPassed()
                        }
                    }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    UiEvent.DismissSheet -> {
                        setState { copy(shouldShowNotice = false) }
                        viewModelScope.launch {
                            delay(NOTICE_INTERVAL_MILLIS)
                            showNoticeIfIntervalPassed()
                        }
                    }
                }
            }
        }

    private fun showNoticeIfIntervalPassed() {
        val now = System.currentTimeMillis()
        val lastShown = lastNoticeShownAtMillis

        if (lastShown == null || now - lastShown >= NOTICE_INTERVAL_MILLIS) {
            lastNoticeShownAtMillis = now
            setState { copy(shouldShowNotice = true) }
        }
    }
}

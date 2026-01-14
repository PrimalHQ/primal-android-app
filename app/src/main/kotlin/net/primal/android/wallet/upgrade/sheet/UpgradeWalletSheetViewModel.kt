package net.primal.android.wallet.upgrade.sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.UserAccount
import net.primal.android.wallet.upgrade.sheet.UpgradeWalletSheetContract.UiEvent
import net.primal.android.wallet.upgrade.sheet.UpgradeWalletSheetContract.UiState

@HiltViewModel
class UpgradeWalletSheetViewModel @Inject constructor(
    val activeAccountStore: ActiveAccountStore,
    val accountsStore: UserAccountsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeActiveAccount()
        observeEvents()
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect { userAccount ->
                setState {
                    copy(
                        shouldShowUpgradeNotice = userAccount != UserAccount.EMPTY &&
                            userAccount.shouldShowUpgradeWalletSheet,
                        activeUserCdnImage = userAccount.avatarCdnImage,
                        activeUserLegendaryCustomization = userAccount.primalLegendProfile?.asLegendaryCustomization(),
                    )
                }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    UiEvent.DismissSheet -> dismissSheet()
                }
            }
        }

    private fun dismissSheet() =
        viewModelScope.launch {
            accountsStore.getAndUpdateAccount(userId = activeAccountStore.activeUserId()) {
                copy(shouldShowUpgradeWalletSheet = false)
            }
        }
}

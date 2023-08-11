package net.primal.android.settings.wallet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.navigation.nwcUrl
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.domain.NostrWalletConnectParseException
import net.primal.android.user.domain.toNostrWalletConnect
import net.primal.android.user.domain.toStringUrl
import net.primal.android.user.repository.UserRepository
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(WalletContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: WalletContract.UiState.() -> WalletContract.UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    init {
        observeWalletState()
    }

    private suspend fun initializeState() {
        try {
            val receivedNWCUrl = savedStateHandle.nwcUrl
            val existingNwc = activeAccountStore.activeUserAccount().nostrWalletConnect

            var nwcUrl: String?
            var isWalletConnected: Boolean

            when {
                receivedNWCUrl != null -> {
                    val nwc = receivedNWCUrl.toNostrWalletConnect()
                    userRepository.updateNostrWalletConnectForUser(activeAccountStore.activeUserId(), nwc)

                    nwcUrl = receivedNWCUrl
                    isWalletConnected = true
                }
                existingNwc != null -> {
                    nwcUrl = existingNwc.toStringUrl()
                    isWalletConnected = true
                }
                else -> {
                    nwcUrl = null
                    isWalletConnected = false
                }
            }

            setState {
                copy(
                    nwcUrl = nwcUrl,
                    isWalletConnected = isWalletConnected
                )
            }
        } catch (e: NostrWalletConnectParseException) {
            // show some error on UI
            return
        }
    }

    private fun observeWalletState() = viewModelScope.launch {
        initializeState()
    }
}
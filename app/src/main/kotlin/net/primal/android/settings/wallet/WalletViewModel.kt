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
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
): ViewModel() {

    // there is a better way to do this for sure
    private val nwcURL = when (savedStateHandle.nwcUrl) {
        null -> null // try and get from storage
        is String -> URLDecoder.decode(savedStateHandle.nwcUrl, Charsets.UTF_8.name())
        else -> null
    }

    private val _state = MutableStateFlow(WalletContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: WalletContract.UiState.() -> WalletContract.UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    init {
        observeWalletState()
    }

    private fun observeWalletState() = viewModelScope.launch {
        loadCurrentWalletState()
    }

    private fun loadCurrentWalletState() {
        setState {
            copy(
                nwcUrl = nwcURL,
                isWalletConnected = nwcURL != null
            )
        }
    }
}
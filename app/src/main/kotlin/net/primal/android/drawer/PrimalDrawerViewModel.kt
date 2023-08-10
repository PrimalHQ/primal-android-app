package net.primal.android.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.active.ActiveThemeStore
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.active.ActiveAccountStore
import net.primal.android.user.active.ActiveUserAccountState
import javax.inject.Inject

@HiltViewModel
class PrimalDrawerViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val accountsStore: UserAccountsStore,
    private val activeThemeStore: ActiveThemeStore,
) : ViewModel() {

    private val _state = MutableStateFlow(
        PrimalDrawerContract.UiState()
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: PrimalDrawerContract.UiState.() -> PrimalDrawerContract.UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val _event: MutableSharedFlow<PrimalDrawerContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: PrimalDrawerContract.UiEvent) {
        viewModelScope.launch { _event.emit(event) }
    }

    init {
        subscribeToEvents()
        observeAccountDataChanges()
        observeActiveAccountChange()
    }

    private fun subscribeToEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is PrimalDrawerContract.UiEvent.ThemeSwitchClick -> invertTheme(it)
            }
        }
    }

    private fun observeActiveAccountChange() = viewModelScope.launch {
        activeAccountStore.activeAccountState
            .filterIsInstance<ActiveUserAccountState.ActiveUserAccount>()
            .collect {
                setState {
                    copy(activeUserAccount = it.data)
                }
            }
    }

    private fun observeAccountDataChanges() = viewModelScope.launch {
        accountsStore.userAccounts
            .mapNotNull { it.find { account -> account.pubkey == activeAccountStore.activeUserId() } }
            .collect {
                setState {
                    copy(activeUserAccount = it)
                }
            }
    }

    private suspend fun invertTheme(event: PrimalDrawerContract.UiEvent.ThemeSwitchClick) {
        val activeTheme = activeThemeStore.userThemeState.firstOrNull()
        val newThemeName = activeTheme?.inverseThemeName
            ?: when (event.isSystemInDarkTheme) {
                true -> PrimalTheme.Sunrise.themeName
                false -> PrimalTheme.Sunset.themeName
            }
        activeThemeStore.setUserTheme(theme = newThemeName)
    }

}
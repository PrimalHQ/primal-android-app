package net.primal.android.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.theme.active.ActiveThemeStore
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager

@HiltViewModel
class PrimalDrawerViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val activeThemeStore: ActiveThemeStore,
    private val subscriptionsManager: SubscriptionsManager,
) : ViewModel() {

    private val _state = MutableStateFlow(
        PrimalDrawerContract.UiState(
            menuItems = buildDrawerMenuItems(userId = activeAccountStore.activeUserId()),
        ),
    )

    val state = _state.asStateFlow()
    private fun setState(reducer: PrimalDrawerContract.UiState.() -> PrimalDrawerContract.UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val events: MutableSharedFlow<PrimalDrawerContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: PrimalDrawerContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        subscribeToEvents()
        observeActiveAccount()
        subscribeToBadgesUpdates()
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is PrimalDrawerContract.UiEvent.ThemeSwitchClick -> invertTheme(it)
                }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        activeUserAccount = it,
                        menuItems = buildDrawerMenuItems(
                            userId = it.pubkey,
                            hasPremium = it.premiumMembership != null,
                        ),
                    )
                }
            }
        }

    private fun subscribeToBadgesUpdates() =
        viewModelScope.launch {
            subscriptionsManager.badges.collect {
                setState {
                    copy(badges = it)
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

    private fun buildDrawerMenuItems(hasPremium: Boolean = false, userId: String) =
        listOf(
            DrawerScreenDestination.Profile,
            DrawerScreenDestination.Premium(hasPremium = hasPremium),
            DrawerScreenDestination.DemoBuyPremium,
            DrawerScreenDestination.Messages,
            DrawerScreenDestination.Bookmarks(userId = userId),
            DrawerScreenDestination.Settings,
            DrawerScreenDestination.SignOut,
        )
}

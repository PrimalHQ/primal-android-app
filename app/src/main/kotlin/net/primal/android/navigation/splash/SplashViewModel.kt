package net.primal.android.navigation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.config.dynamic.AppConfigUpdater
import net.primal.android.navigation.splash.SplashContract.SideEffect
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val appConfigUpdater: AppConfigUpdater,
) : ViewModel() {

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) =
        viewModelScope.launch {
            _effect.send(effect)
        }

    init {
        fetchLatestAppConfig()
        dispatchInitialScreen()
        subscribeToNoAccountsState()
    }

    private fun fetchLatestAppConfig() =
        viewModelScope.launch {
            appConfigUpdater.updateAppConfigOrFailSilently()
        }

    private fun dispatchInitialScreen() =
        viewModelScope.launch {
            val activeUserAccountState = activeAccountStore.activeAccountState.first()

            setEffect(
                effect = when (activeUserAccountState) {
                    ActiveUserAccountState.NoUserAccount -> SideEffect.NoActiveAccount
                    is ActiveUserAccountState.ActiveUserAccount -> SideEffect.ActiveAccount(
                        userPubkey = activeUserAccountState.data.pubkey,
                    )
                },
            )
        }

    private fun subscribeToNoAccountsState() =
        viewModelScope.launch {
            activeAccountStore.activeAccountState.filterIsInstance<ActiveUserAccountState.NoUserAccount>()
                .collect {
                    setEffect(effect = SideEffect.NoActiveAccount)
                }
        }
}

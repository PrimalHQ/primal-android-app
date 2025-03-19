package net.primal.android.navigation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.config.AppConfigHandler

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val appConfigHandler: AppConfigHandler,
) : ViewModel() {

    private val _isAuthCheckComplete = MutableStateFlow(false)
    val isAuthCheckComplete = _isAuthCheckComplete

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn

    init {
        checkAuthState()
        fetchLatestAppConfig()
    }

    private fun checkAuthState() =
        viewModelScope.launch {
            _isLoggedIn.value = activeAccountStore.activeUserId().isNotEmpty()
            _isAuthCheckComplete.value = true
        }

    private fun fetchLatestAppConfig() =
        viewModelScope.launch {
            appConfigHandler.updateAppConfigOrFailSilently()
        }
}

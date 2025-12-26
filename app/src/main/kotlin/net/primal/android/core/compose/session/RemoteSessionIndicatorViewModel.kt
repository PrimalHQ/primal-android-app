package net.primal.android.core.compose.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.push.PushNotificationsTokenUpdater
import net.primal.android.core.service.PrimalRemoteSignerService
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.asKeyPair
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.account.repository.SessionRepository

@HiltViewModel
class RemoteSessionIndicatorViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val credentialsStore: CredentialsStore,
    private val accountsStore: UserAccountsStore,
    private val activeAccountStore: ActiveAccountStore,
    private val pushNotificationsTokenUpdater: PushNotificationsTokenUpdater,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(RemoteSessionIndicatorContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: RemoteSessionIndicatorContract.UiState.() -> RemoteSessionIndicatorContract.UiState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<RemoteSessionIndicatorContract.UiEvent>()
    fun setEvent(event: RemoteSessionIndicatorContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    private var sessionObserverJob: Job? = null

    init {
        observeRemoteSignerServiceState()
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is RemoteSessionIndicatorContract.UiEvent.PushNotificationsToggled ->
                        updatePushNotificationsEnabled(it.enabled)
                }
            }
        }

    private fun updatePushNotificationsEnabled(value: Boolean) =
        viewModelScope.launch {
            withContext(dispatcherProvider.io()) {
                accountsStore.getAndUpdateAccount(activeAccountStore.activeUserId()) {
                    copy(pushNotificationsEnabled = value)
                }
                pushNotificationsTokenUpdater.updateTokenForAllUsers()
            }
        }

    private fun observeRemoteSignerServiceState() =
        viewModelScope.launch {
            PrimalRemoteSignerService.isServiceRunning.collect { isRunning ->
                setState { copy(isRemoteSessionActive = isRunning) }
                if (isRunning) {
                    startObservingActiveSession()
                } else {
                    stopObservingActiveSession()
                }
            }
        }

    private fun startObservingActiveSession() {
        if (sessionObserverJob?.isActive == true) return

        sessionObserverJob = viewModelScope.launch {
            val pubkey = credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair().pubKey
            sessionRepository.observeOngoingSessions(signerPubKey = pubkey).collect { sessions ->
                val lastSession = sessions.maxByOrNull { it.sessionStartedAt }
                setState {
                    copy(
                        activeAppName = lastSession?.name,
                        activeAppIconUrl = lastSession?.image,
                    )
                }
            }
        }
    }

    private fun stopObservingActiveSession() {
        sessionObserverJob?.cancel()
        sessionObserverJob = null
        setState {
            copy(
                activeAppName = null,
                activeAppIconUrl = null,
            )
        }
    }
}

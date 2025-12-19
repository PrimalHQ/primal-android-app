package net.primal.android.signer.provider.connect

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.errors.UiError
import net.primal.android.drawer.multiaccount.model.asUserAccountUi
import net.primal.android.signer.provider.callingPackageOrThrow
import net.primal.android.signer.provider.connect.AndroidConnectContract.SideEffect
import net.primal.android.signer.provider.connect.AndroidConnectContract.UiEvent
import net.primal.android.signer.provider.connect.AndroidConnectContract.UiState
import net.primal.android.signer.provider.signerRequestedPermissionsJsonOrNull
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.CredentialType
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.account.service.LocalSignerService
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp

@HiltViewModel
@Suppress("unused")
class AndroidConnectViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountsStore: UserAccountsStore,
    private val credentialsStore: CredentialsStore,
    private val localSignerService: LocalSignerService,
) : ViewModel() {

    private val permissionsJson: String? = savedStateHandle.signerRequestedPermissionsJsonOrNull
    private val callingPackage: String = savedStateHandle.callingPackageOrThrow

    private val _state = MutableStateFlow(UiState(appPackageName = callingPackage))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }
    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeAccounts()
        observeEvents()
    }

    private fun observeAccounts() {
        viewModelScope.launch {
            accountsStore.userAccounts.collect { userAccounts ->
                val allCredentials = credentialsStore.credentials.value
                val nsecOnlyUserAccounts = userAccounts.filter { userAccount ->
                    val credential = allCredentials.find { credential ->
                        credential.npub == userAccount.pubkey.hexToNpubHrp()
                    }
                    credential?.type == CredentialType.PrivateKey
                }

                val accounts = nsecOnlyUserAccounts
                    .sortedByDescending { it.lastAccessedAt }
                    .map { it.asUserAccountUi() }

                setState { copy(accounts = accounts) }
            }
        }
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.ConnectUser -> addNewApp(userId = it.userId, trustLevel = it.trustLevel)
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun addNewApp(userId: String, trustLevel: TrustLevel) =
        viewModelScope.launch {
            setState { copy(connecting = true) }
            val app = LocalApp(
                identifier = "$callingPackage:$userId",
                packageName = callingPackage,
                userPubKey = userId,
                trustLevel = trustLevel,
                permissions = emptyList(),
            )

            localSignerService.addNewApp(app)
                .onSuccess {
                    setEffect(SideEffect.ConnectionSuccess(userId = userId))
                }
                .onFailure {
                    setState { copy(error = UiError.GenericError()) }
                    setEffect(SideEffect.ConnectionFailure(error = it))
                }
            setState { copy(connecting = false) }
        }
}

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
import net.primal.android.signer.provider.parser.SignerIntentParser
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
    private val localSignerService: LocalSignerService,
    private val intentParser: SignerIntentParser,
    private val accountsStore: UserAccountsStore,
    private val credentialsStore: CredentialsStore,
) : ViewModel() {

    private val permissionsJson: String? = savedStateHandle.signerRequestedPermissionsJsonOrNull
    private val callingPackage: String = savedStateHandle.callingPackageOrThrow

    private val _state = MutableStateFlow(
        AndroidConnectContract.UiState(
            appPackageName = callingPackage,
            appName = callingPackage,
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: AndroidConnectContract.UiState.() -> AndroidConnectContract.UiState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<AndroidConnectContract.UiEvent>()
    fun setEvent(event: AndroidConnectContract.UiEvent) = viewModelScope.launch { events.emit(event) }
    private val _effects = Channel<AndroidConnectContract.SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: AndroidConnectContract.SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        resolveAppInfo()
        observeAccounts()
        observeEvents()
    }

    private fun resolveAppInfo() {
        val (label, icon) = intentParser.getAppLabelAndIcon(callingPackage)
        setState {
            copy(
                appName = label ?: callingPackage,
                appIcon = icon,
            )
        }
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
                    is AndroidConnectContract.UiEvent.ConnectUser -> {
                        addNewApp(
                            userId = it.userId,
                            appName = state.value.appName,
                            trustLevel = it.trustLevel,
                        )
                    }
                    AndroidConnectContract.UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun addNewApp(
        userId: String,
        appName: String,
        trustLevel: TrustLevel,
    ) = viewModelScope.launch {
        setState { copy(connecting = true) }
        val app = LocalApp(
            identifier = "$callingPackage:$userId",
            packageName = callingPackage,
            userPubKey = userId,
            trustLevel = trustLevel,
            permissions = emptyList(),
        )

        localSignerService.addNewApp(app).onSuccess {
            setEffect(AndroidConnectContract.SideEffect.ConnectionSuccess(userId = userId))
        }.onFailure {
            setState { copy(error = UiError.GenericError()) }
            setEffect(AndroidConnectContract.SideEffect.ConnectionFailure(error = it))
        }
        setState { copy(connecting = false) }
    }
}

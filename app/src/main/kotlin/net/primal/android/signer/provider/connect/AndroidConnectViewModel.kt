package net.primal.android.signer.provider.connect

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.signer.provider.callingPackageOrThrow
import net.primal.android.signer.provider.signerRequestedPermissionsJsonOrNull
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.account.service.LocalSignerService

@HiltViewModel
@OptIn(ExperimentalUuidApi::class)
class AndroidConnectViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val localSignerService: LocalSignerService,
) : ViewModel() {

    private val permissionsJson: String? = savedStateHandle.signerRequestedPermissionsJsonOrNull
    private val callingPackage: String = savedStateHandle.callingPackageOrThrow

    private val _state = MutableStateFlow(
        AndroidConnectContract.UiState(
            appPackageName = callingPackage,
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
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is AndroidConnectContract.UiEvent.ConnectUser -> {
                        addNewApp(
                            userId = it.userId,
                            appName = "Demo App",
                            trustLevel = TrustLevel.Full,
                        )
                    }
                }
            }
        }

    private fun addNewApp(
        userId: String,
        appName: String,
        trustLevel: TrustLevel,
    ) = viewModelScope.launch {
        val app = LocalApp(
            identifier = "$callingPackage:$userId",
            packageName = callingPackage,
            userPubKey = userId,
            image = null,
            name = appName,
            trustLevel = trustLevel,
            permissions = emptyList(),
        )

        localSignerService.addNewApp(app).onSuccess {
            setEffect(AndroidConnectContract.SideEffect.ConnectionSuccess(userId = userId))
        }.onFailure {
            setEffect(AndroidConnectContract.SideEffect.ConnectionFailure(error = it))
        }
    }
}

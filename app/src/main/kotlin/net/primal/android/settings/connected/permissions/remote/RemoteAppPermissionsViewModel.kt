package net.primal.android.settings.connected.permissions.remote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import net.primal.android.core.errors.UiError
import net.primal.android.navigation.clientPubKeyOrThrow
import net.primal.android.settings.connected.model.asPermissionGroupUi
import net.primal.android.settings.connected.permissions.remote.RemoteAppPermissionsContract.UiEvent
import net.primal.android.settings.connected.permissions.remote.RemoteAppPermissionsContract.UiState
import net.primal.core.utils.onFailure
import net.primal.domain.account.model.AppPermissionAction
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.domain.account.repository.SessionRepository

@HiltViewModel
class RemoteAppPermissionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val connectionRepository: ConnectionRepository,
    private val sessionRepository: SessionRepository,
    private val permissionsRepository: PermissionsRepository,
) : ViewModel() {

    private val clientPubKey: String = savedStateHandle.clientPubKeyOrThrow

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeAppConnection()
        observePermissions()
        observeLastSession()
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.UpdatePermission -> updatePermission(
                        permissionIds = event.permissionIds,
                        action = event.action,
                    )

                    UiEvent.Retry -> observePermissions()
                    UiEvent.DismissError -> setState { copy(error = null) }
                    UiEvent.ResetPermissions -> resetPermissions()
                }
            }
        }

    private fun observeLastSession() {
        viewModelScope.launch {
            sessionRepository.observeSessionsByAppIdentifier(appIdentifier = clientPubKey).collect { sessions ->
                setState {
                    copy(appLastSessionAt = sessions.firstOrNull()?.sessionStartedAt)
                }
            }
        }
    }

    private fun observeAppConnection() {
        viewModelScope.launch {
            connectionRepository.observeConnection(clientPubKey).collect { connection ->
                setState {
                    copy(
                        appName = connection?.name,
                        appIconUrl = connection?.image,
                    )
                }
            }
        }
    }

    private fun observePermissions() {
        permissionsRepository.observePermissions(clientPubKey)
            .onStart { setState { copy(loading = true, error = null) } }
            .onEach { groups ->
                setState {
                    copy(
                        permissions = groups.map { it.asPermissionGroupUi() },
                        loading = false,
                    )
                }
            }
            .catch { error ->
                setState {
                    copy(
                        error = UiError.GenericError(error.message),
                        loading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updatePermission(permissionIds: List<String>, action: AppPermissionAction) {
        viewModelScope.launch {
            permissionsRepository.updatePermissionsAction(
                permissionIds = permissionIds,
                clientPubKey = clientPubKey,
                action = action,
            ).onFailure {
                setState { copy(error = UiError.GenericError(it.message)) }
            }
        }
    }

    private fun resetPermissions() {
        viewModelScope.launch {
            setState { copy(loading = true) }
            permissionsRepository.resetPermissionsToDefault(clientPubKey = clientPubKey)
                .onFailure {
                    setState { copy(error = UiError.GenericError(it.message), loading = false) }
                }
        }
    }
}

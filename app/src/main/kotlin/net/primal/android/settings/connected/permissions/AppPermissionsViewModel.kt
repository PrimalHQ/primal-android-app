package net.primal.android.settings.connected.permissions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.errors.UiError
import net.primal.android.navigation.clientPubKeyOrThrow
import net.primal.android.settings.connected.model.asPermissionGroupUi
import net.primal.android.settings.connected.permissions.AppPermissionsContract.UiEvent
import net.primal.android.settings.connected.permissions.AppPermissionsContract.UiState
import net.primal.core.utils.fold
import net.primal.core.utils.onFailure
import net.primal.domain.account.model.PermissionAction
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.domain.account.repository.SessionRepository

@HiltViewModel
class AppPermissionsViewModel @Inject constructor(
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
            sessionRepository.observeSessionsByClientPubKey(clientPubKey).collect { sessions ->
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
        viewModelScope.launch {
            setState { copy(loading = true, error = null) }
            permissionsRepository.observePermissions(clientPubKey).fold(
                onSuccess = { permissionsFlow ->
                    permissionsFlow.collect { groups ->
                        setState {
                            copy(
                                permissions = groups.map { it.asPermissionGroupUi() },
                                loading = false,
                            )
                        }
                    }
                },
                onFailure = {
                    setState {
                        copy(
                            error = UiError.GenericError(it.message),
                            loading = false,
                        )
                    }
                },
            )
        }
    }

    private fun updatePermission(permissionIds: List<String>, action: PermissionAction) {
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

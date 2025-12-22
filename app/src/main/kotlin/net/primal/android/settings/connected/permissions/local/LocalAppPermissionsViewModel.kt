package net.primal.android.settings.connected.permissions.local

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
import net.primal.android.navigation.identifierOrThrow
import net.primal.android.settings.connected.model.asPermissionGroupUi
import net.primal.android.settings.connected.permissions.local.LocalAppPermissionsContract.UiEvent
import net.primal.android.settings.connected.permissions.local.LocalAppPermissionsContract.UiState
import net.primal.core.utils.onFailure
import net.primal.domain.account.model.PermissionAction
import net.primal.domain.account.repository.LocalAppRepository
import net.primal.domain.account.repository.PermissionsRepository

@HiltViewModel
class LocalAppPermissionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val localAppRepository: LocalAppRepository,
    private val permissionsRepository: PermissionsRepository,
) : ViewModel() {

    private val identifier: String = savedStateHandle.identifierOrThrow

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeApp()
        observePermissions()
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

    private fun observeApp() {
        viewModelScope.launch {
            localAppRepository.observeApp(identifier).collect { app ->
                setState { copy(packageName = app?.packageName) }
            }
        }
    }

    private fun observePermissions() {
        viewModelScope.launch {
            permissionsRepository.observePermissions(clientPubKey = identifier)
                .collect { groups ->
                    setState {
                        copy(
                            permissions = groups.map { it.asPermissionGroupUi() },
                            loading = false,
                        )
                    }
                }
        }
    }

    private fun updatePermission(permissionIds: List<String>, action: PermissionAction) {
        viewModelScope.launch {
            permissionsRepository.updatePermissionsAction(
                permissionIds = permissionIds,
                clientPubKey = identifier,
                action = action,
            ).onFailure {
                setState { copy(error = UiError.GenericError(it.message)) }
            }
        }
    }

    private fun resetPermissions() {
        viewModelScope.launch {
            setState { copy(loading = true) }
            permissionsRepository.resetPermissionsToDefault(clientPubKey = identifier)
                .onFailure {
                    setState { copy(error = UiError.GenericError(it.message), loading = false) }
                }
        }
    }
}

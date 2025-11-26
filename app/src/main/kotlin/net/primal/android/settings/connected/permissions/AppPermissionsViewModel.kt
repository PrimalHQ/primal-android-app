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
import net.primal.android.navigation.connectionIdOrThrow
import net.primal.android.settings.connected.model.PermissionUi
import net.primal.android.settings.connected.permissions.AppPermissionsContract.UiEvent
import net.primal.android.settings.connected.permissions.AppPermissionsContract.UiState
import net.primal.domain.account.model.PermissionAction
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionRepository

@HiltViewModel
class AppPermissionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val connectionRepository: ConnectionRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val connectionId: String = savedStateHandle.connectionIdOrThrow

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        loadPermissions()
        observeLastSession()
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.ChangePermission -> updatePermission(event.permissionId, event.action)
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun observeLastSession() {
        viewModelScope.launch {
            sessionRepository.observeSessionsByConnectionId(connectionId).collect { sessions ->
                setState {
                    copy(appLastSessionAt = sessions.firstOrNull()?.sessionStartedAt)
                }
            }
        }
    }

    private fun loadPermissions() {
        viewModelScope.launch {
            connectionRepository.observeConnection(connectionId).collect { connection ->
                if (connection != null) {
                    val dbPermissions = connection.permissions.associateBy { it.permissionId }

                    val uiPermissions = KNOWN_PERMISSIONS.map { (id, title) ->
                        val existingAction = dbPermissions[id]?.action ?: PermissionAction.Ask
                        PermissionUi(
                            permissionId = id,
                            title = title,
                            action = existingAction,
                        )
                    }

                    setState {
                        copy(
                            appName = connection.name,
                            appIconUrl = connection.image,
                            permissions = uiPermissions,
                            loading = false,
                        )
                    }
                }
            }
        }
    }

    private suspend fun updatePermission(permissionId: String, action: PermissionAction) {
        setState {
            copy(
                permissions = permissions.map {
                    if (it.permissionId == permissionId) it.copy(action = action) else it
                },
            )
        }

        runCatching {
            connectionRepository.updatePermission(connectionId, permissionId, action)
        }
    }

    companion object {
        private val KNOWN_PERMISSIONS = listOf(
            "get_public_key" to "Read Public Key",
            "sign_event:0" to "Update Profile",
            "sign_event:1" to "Publish Note",
            "sign_event:30023" to "Publish Article",
            "sign_event:3" to "Update Follow List",
            "sign_event:7" to "Public Reaction",
            "sign_event:6" to "Repost",
            "sign_event:9734" to "Zap",
            "sign_event:4" to "Send Direct Message",
            "sign_event:10000" to "Update Mute List",
            "sign_event:30001" to "Add Bookmark",
            "sign_event:1063" to "Upload File",
            "nip04_encrypt" to "Encrypt",
            "nip04_decrypt" to "Decrypt",
            "connect" to "Authenticate",
        )
    }
}

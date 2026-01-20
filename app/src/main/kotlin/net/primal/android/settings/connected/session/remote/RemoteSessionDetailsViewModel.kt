package net.primal.android.settings.connected.session.remote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.navigation.sessionIdOrThrow
import net.primal.android.settings.connected.model.asSessionEventUi
import net.primal.core.utils.onSuccess
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.domain.account.repository.SessionEventRepository
import net.primal.domain.account.repository.SessionRepository

@HiltViewModel
class RemoteSessionDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val sessionEventRepository: SessionEventRepository,
    private val permissionsRepository: PermissionsRepository,
) : ViewModel() {

    private val sessionId: String = savedStateHandle.sessionIdOrThrow

    private val _state = MutableStateFlow(
        RemoteSessionDetailsContract.UiState(
            sessionId = sessionId,
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: RemoteSessionDetailsContract.UiState.() -> RemoteSessionDetailsContract.UiState) =
        _state.getAndUpdate(reducer)

    init {
        fetchPermissionsNamingMap()
        observeSession()
        observeSessionEvents()
    }

    private fun fetchPermissionsNamingMap() =
        viewModelScope.launch {
            permissionsRepository.getNamingMap()
                .onSuccess { namingMap ->
                    setState { copy(namingMap = namingMap) }
                }
        }

    private fun observeSession() =
        viewModelScope.launch {
            sessionRepository.observeRemoteSession(sessionId = sessionId).collect { session ->
                setState {
                    copy(
                        loading = false,
                        appName = session?.name,
                        appIconUrl = session?.image,
                        sessionStartedAt = session?.sessionStartedAt,
                    )
                }
            }
        }

    private fun observeSessionEvents() =
        viewModelScope.launch {
            sessionEventRepository.observeCompletedEventsForRemoteSession(sessionId = sessionId).collect { events ->
                setState {
                    copy(sessionEvents = events.map { it.asSessionEventUi() })
                }
            }
        }
}

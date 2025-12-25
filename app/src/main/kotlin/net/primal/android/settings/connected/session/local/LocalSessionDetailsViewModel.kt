package net.primal.android.settings.connected.session.local

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
class LocalSessionDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val sessionEventRepository: SessionEventRepository,
    private val permissionsRepository: PermissionsRepository,
) : ViewModel() {

    private val sessionId: String = savedStateHandle.sessionIdOrThrow

    private val _state = MutableStateFlow(LocalSessionDetailsContract.UiState(sessionId = sessionId))
    val state = _state.asStateFlow()

    private fun setState(reducer: LocalSessionDetailsContract.UiState.() -> LocalSessionDetailsContract.UiState) =
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
            sessionRepository.observeLocalSession(sessionId = sessionId).collect { session ->
                setState {
                    copy(
                        loading = false,
                        appPackageName = session?.appPackageName,
                        sessionStartedAt = session?.sessionStartedAt,
                    )
                }
            }
        }

    private fun observeSessionEvents() =
        viewModelScope.launch {
            sessionEventRepository.observeCompletedLocalEventsForSession(sessionId = sessionId).collect { events ->
                setState {
                    copy(sessionEvents = events.map { it.asSessionEventUi() })
                }
            }
        }
}

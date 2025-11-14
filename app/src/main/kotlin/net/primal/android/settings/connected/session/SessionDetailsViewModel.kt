package net.primal.android.settings.connected.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.navigation.connectionIdOrThrow
import net.primal.android.navigation.sessionIdOrThrow
import net.primal.android.settings.connected.model.asSessionEventUi
import net.primal.android.settings.connected.session.SessionDetailsContract.SideEffect
import net.primal.android.settings.connected.session.SessionDetailsContract.UiEvent
import net.primal.domain.account.repository.SessionEventRepository
import net.primal.domain.account.repository.SessionRepository

@HiltViewModel
class SessionDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val sessionEventRepository: SessionEventRepository,
) : ViewModel() {

    private val connectionId: String = savedStateHandle.connectionIdOrThrow
    private val sessionId: String = savedStateHandle.sessionIdOrThrow

    private val _state = MutableStateFlow(SessionDetailsContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: SessionDetailsContract.UiState.() -> SessionDetailsContract.UiState) =
        _state.getAndUpdate(reducer)

    private val _effect = Channel<SideEffect>()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    init {
        observeSession()
        observeSessionEvents()
    }

    fun setEvent(event: UiEvent) {
        when (event) {
            is UiEvent.EventClick -> {
                setEffect(
                    SideEffect.NavigateToEventDetails(
                        connectionId = this.connectionId,
                        sessionId = this.sessionId,
                        eventId = event.eventId,
                    ),
                )
            }
        }
    }

    private fun observeSession() =
        viewModelScope.launch {
            sessionRepository.observeSession(sessionId = sessionId).collect { session ->
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
            sessionEventRepository.observeEventsForSession(sessionId = sessionId).collect { events ->
                setState {
                    copy(sessionEvents = events.map { it.asSessionEventUi() })
                }
            }
        }
}

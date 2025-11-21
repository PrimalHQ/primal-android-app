package net.primal.android.nostrconnect.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.nostrconnect.model.asUi
import net.primal.android.nostrconnect.permissions.PermissionsContract.UiEvent
import net.primal.android.nostrconnect.permissions.PermissionsContract.UiState
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.asKeyPair
import net.primal.core.utils.getIfTypeOrNull
import net.primal.core.utils.onSuccess
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.account.model.UserChoice
import net.primal.domain.account.repository.SessionEventRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.nostr.NostrUnsignedEvent

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val sessionEventRepository: SessionEventRepository,
    private val credentialsStore: CredentialsStore,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeActiveSessions()
        observeSessionEventsPendingUserAction()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    UiEvent.DismissSheet -> setState { copy(bottomSheetVisibility = false) }
                    UiEvent.SelectAll -> setState {
                        copy(selectedEventIds = sessionEvents.map { it.eventId }.toSet())
                    }

                    UiEvent.DeselectAll -> setState { copy(selectedEventIds = emptySet()) }
                    is UiEvent.SelectEvent -> setState { copy(selectedEventIds = selectedEventIds + event.eventId) }
                    is UiEvent.DeselectEvent -> setState { copy(selectedEventIds = selectedEventIds - event.eventId) }

                    is UiEvent.AllowSelected -> respondToEvents(
                        eventIds = state.value.selectedEventIds,
                        choice = if (event.alwaysAllow) UserChoice.AlwaysAllow else UserChoice.Allow,
                    )

                    is UiEvent.RejectSelected -> respondToEvents(
                        eventIds = state.value.selectedEventIds,
                        choice = if (event.alwaysReject) UserChoice.AlwaysReject else UserChoice.Reject,
                    )

                    is UiEvent.OpenEventDetails -> handleOpenEventDetails(event.eventId)

                    UiEvent.CloseEventDetails -> setState { copy(eventDetailsUnsignedEvent = null) }
                }
            }
        }

    private fun handleOpenEventDetails(eventId: String) {
        val sessionEvent = state.value.sessionEvents.find { it.eventId == eventId }

        val nostrUnsignedEvent = sessionEvent.getIfTypeOrNull(SessionEvent.SignEvent::unsignedNostrEventJson)
            ?.let {
                runCatching { NostrJsonEncodeDefaults.decodeFromString<NostrUnsignedEvent>(it) }.getOrNull()
            }

        setState { copy(eventDetailsUnsignedEvent = nostrUnsignedEvent) }
    }

    private fun respondToEvents(eventIds: Set<String>, choice: UserChoice) =
        viewModelScope.launch {
            setState { copy(responding = true) }
            sessionEventRepository
                .respondToEvents(eventIdToUserChoice = eventIds.map { it to choice })
                .onSuccess {
                    setState { copy(selectedEventIds = selectedEventIds - eventIds) }
                }
            setState { copy(responding = false) }
        }

    private fun observeActiveSessions() =
        viewModelScope.launch {
            sessionRepository.observeActiveSessions(
                signerPubKey = credentialsStore.getOrCreateInternalSignerCredentials()
                    .asKeyPair().pubKey,
            ).collect { sessions ->
                setState { copy(activeSessions = sessions.associate { it.sessionId to it.asUi(null) }) }
            }
        }

    private fun observeSessionEventsPendingUserAction() =
        viewModelScope.launch {
            sessionEventRepository.observeEventsPendingUserAction(
                signerPubKey = credentialsStore.getOrCreateInternalSignerCredentials()
                    .asKeyPair().pubKey,
            ).collect { events ->
                val requestsQueue = events.groupBy(keySelector = { it.sessionId })
                    .mapNotNull { (sessionId, events) ->
                        val session = sessionRepository
                            .getSession(sessionId = sessionId).getOrNull() ?: return@mapNotNull null

                        session.asUi(null) to events
                    }

                setState {
                    copy(
                        bottomSheetVisibility = events.isNotEmpty(),
                        requestQueue = requestsQueue,
                    )
                }
            }
        }
}

package net.primal.android.nostrconnect.approvals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.nostrconnect.approvals.PermissionsContract.UiEvent
import net.primal.android.nostrconnect.approvals.PermissionsContract.UiState
import net.primal.android.nostrconnect.model.asUi
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.asKeyPair
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onSuccess
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.account.model.SessionEventUserChoice
import net.primal.domain.account.model.UserChoice
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.domain.account.repository.SessionEventRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val sessionEventRepository: SessionEventRepository,
    private val permissionsRepository: PermissionsRepository,
    private val credentialsStore: CredentialsStore,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        fetchPermissionsNamingMap()
        observeActiveSessions()
        observeSessionEventsPendingUserAction()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    UiEvent.DismissSheet -> setState { copy(bottomSheetVisibility = false) }

                    is UiEvent.Allow -> respondToEvents(
                        eventIds = event.eventIds,
                        choice = if (event.alwaysAllow) UserChoice.AlwaysAllow else UserChoice.Allow,
                    )

                    is UiEvent.Reject -> respondToEvents(
                        eventIds = event.eventIds,
                        choice = if (event.alwaysReject) UserChoice.AlwaysReject else UserChoice.Reject,
                    )

                    is UiEvent.OpenEventDetails -> handleOpenEventDetails(event.eventId)

                    UiEvent.CloseEventDetails -> setState { copy(eventDetailsSessionEvent = null) }
                }
            }
        }

    private fun fetchPermissionsNamingMap() =
        viewModelScope.launch {
            permissionsRepository.getNamingMap()
                .onSuccess { setState { copy(permissionsMap = it) } }
        }

    private fun handleOpenEventDetails(eventId: String) {
        val sessionEvent = state.value.sessionEvents.find { it.eventId == eventId }
        viewModelScope.launch {
            var parsedSigned: NostrEvent? = null
            var parsedUnsigned: NostrUnsignedEvent? = null

            if (sessionEvent is SessionEvent.SignEvent) {
                withContext(dispatcherProvider.io()) {
                    sessionEvent.signedNostrEventJson?.let { json ->
                        parsedSigned = runCatching {
                            NostrJsonEncodeDefaults.decodeFromString<NostrEvent>(json)
                        }.getOrNull()
                    }

                    parsedUnsigned = runCatching {
                        NostrJsonEncodeDefaults.decodeFromString<NostrUnsignedEvent>(
                            sessionEvent.unsignedNostrEventJson,
                        )
                    }.getOrNull()
                }
            }

            setState {
                copy(
                    eventDetailsSessionEvent = sessionEvent,
                    parsedSignedEvent = parsedSigned,
                    parsedUnsignedEvent = parsedUnsigned,
                )
            }
        }
    }

    private fun respondToEvents(eventIds: List<String>, choice: UserChoice) =
        viewModelScope.launch {
            setState { copy(responding = true) }
            val userChoices = eventIds.map { SessionEventUserChoice(sessionEventId = it, userChoice = choice) }
            sessionEventRepository.respondToRemoteEvents(userChoices = userChoices)
            setState { copy(responding = false) }
        }

    private fun observeActiveSessions() =
        viewModelScope.launch {
            sessionRepository.observeOngoingSessions(
                signerPubKey = credentialsStore.getOrCreateInternalSignerCredentials()
                    .asKeyPair().pubKey,
            ).collect { sessions ->
                setState { copy(activeSessions = sessions.associate { it.sessionId to it.asUi(null) }) }
            }
        }

    private fun observeSessionEventsPendingUserAction() =
        viewModelScope.launch {
            sessionEventRepository.observeEventsPendingUserActionForRemoteSigner(
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
                        bottomSheetVisibility = requestsQueue.isNotEmpty(),
                        requestQueue = requestsQueue,
                    )
                }
            }
        }
}

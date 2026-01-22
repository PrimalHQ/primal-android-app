package net.primal.android.signer.provider.approvals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.signer.provider.approvals.PermissionRequestsContract.SideEffect
import net.primal.android.signer.provider.approvals.PermissionRequestsContract.UiEvent
import net.primal.android.signer.provider.approvals.PermissionRequestsContract.UiState
import net.primal.android.signer.provider.localSignerMethodOrThrow
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.data.account.repository.service.LocalSignerError
import net.primal.data.account.repository.service.LocalSignerService
import net.primal.data.account.signer.local.model.LocalSignerMethod
import net.primal.data.account.signer.local.model.LocalSignerMethodResponse
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.account.model.SessionEventUserChoice
import net.primal.domain.account.model.UserChoice
import net.primal.domain.account.repository.LocalAppRepository
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.domain.account.repository.SessionEventRepository
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

@HiltViewModel
@OptIn(ExperimentalUuidApi::class)
class PermissionRequestsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val localSignerService: LocalSignerService,
    private val dispatcherProvider: DispatcherProvider,
    private val permissionsRepository: PermissionsRepository,
    private val sessionEventRepository: SessionEventRepository,
    private val localAppRepository: LocalAppRepository,
) : ViewModel() {

    private val initialMethod: LocalSignerMethod = savedStateHandle.localSignerMethodOrThrow

    private val _state = MutableStateFlow(UiState(callingPackage = initialMethod.packageName))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
        observeSessionEventsPendingUserAction()
        fetchPermissionsNamingMap()
        observeAppName()
        onNewLocalSignerMethod(initialMethod)
    }

    fun onNewLocalSignerMethod(method: LocalSignerMethod) =
        viewModelScope.launch {
            Napier.d(tag = "LocalSignerForeground") { "We got $method." }
            localSignerService.processMethodOrAddToPending(method = method)
                .onFailure { error ->
                    if (error is LocalSignerError.AppNotFound) {
                        setEffect(SideEffect.InvalidRequest)
                    }
                }
        }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.Allow -> respondToEvents(
                        selectedEventIds = it.eventIds,
                        choice = if (it.alwaysAllow) UserChoice.AlwaysAllow else UserChoice.Allow,
                    )

                    is UiEvent.Reject -> respondToEvents(
                        selectedEventIds = it.eventIds,
                        choice = if (it.alwaysReject) UserChoice.AlwaysReject else UserChoice.Reject,
                    )

                    UiEvent.RejectAll -> respondToEvents(
                        selectedEventIds = _state.value.requestQueue.map { request -> request.eventId },
                        choice = UserChoice.Reject,
                    )

                    is UiEvent.OpenEventDetails -> handleOpenEventDetails(it.eventId)
                    UiEvent.CloseEventDetails -> setState { copy(eventDetailsSessionEvent = null) }
                }
            }
        }
    }

    private fun observeSessionEventsPendingUserAction() {
        viewModelScope.launch {
            sessionEventRepository.observeEventsPendingUserActionForLocalApp(
                appIdentifier = initialMethod.getIdentifier(),
            ).collect { events ->
                val previousQueueNotEmpty = state.value.requestQueue.isNotEmpty()
                setState { copy(requestQueue = events) }
                if (events.isEmpty() && previousQueueNotEmpty) {
                    sendResponses()
                    Napier.d(tag = "LocalSignerForeground") {
                        "There are no new pending events, but the state has events in requestQueue.\n" +
                            "Sending responses immediately."
                    }
                }
            }
        }
    }

    private fun fetchPermissionsNamingMap() =
        viewModelScope.launch {
            permissionsRepository.getNamingMap()
                .onSuccess { setState { copy(permissionsMap = it) } }
        }

    private fun observeAppName() =
        viewModelScope.launch {
            localAppRepository.observeApp(initialMethod.getIdentifier())
                .collect { app ->
                    setState { copy(appName = app?.name) }
                }
        }

    private fun handleOpenEventDetails(eventId: String) {
        val sessionEvent = state.value.requestQueue.find { it.eventId == eventId }
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

    private fun respondToEvents(selectedEventIds: List<String>, choice: UserChoice) =
        viewModelScope.launch {
            setState { copy(responding = true) }

            val allPendingEvents = state.value.requestQueue
            val unselectedEventIds = allPendingEvents.map { it.eventId } - selectedEventIds.toSet()
            val unselectedChoices = unselectedEventIds.map { id ->
                SessionEventUserChoice(
                    sessionEventId = id,
                    userChoice = UserChoice.Reject,
                )
            }

            val selectedChoices = selectedEventIds.map { id ->
                SessionEventUserChoice(
                    sessionEventId = id,
                    userChoice = choice,
                )
            }

            localSignerService.respondToUserActions(eventChoices = selectedChoices + unselectedChoices)
            sendResponses()
            setState { copy(responding = false) }
        }

    private fun sendResponses() {
        val responses = localSignerService.getAllMethodResponses()

        setEffect(
            SideEffect.RequestsCompleted(
                approved = responses.filterIsInstance<LocalSignerMethodResponse.Success>(),
                rejected = responses.filterIsInstance<LocalSignerMethodResponse.Error>(),
            ),
        )
    }
}

package net.primal.android.signer.provider.approvals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import net.primal.core.utils.onSuccess
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.account.model.SessionEventUserChoice
import net.primal.domain.account.model.UserChoice
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.domain.account.service.LocalSignerService
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent
import timber.log.Timber

@HiltViewModel
@OptIn(ExperimentalUuidApi::class)
class PermissionRequestsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val localSignerService: LocalSignerService,
    private val dispatcherProvider: DispatcherProvider,
    private val permissionsRepository: PermissionsRepository,
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
    }

    private fun fetchPermissionsNamingMap() =
        viewModelScope.launch {
            permissionsRepository.getNamingMap()
                .onSuccess { setState { copy(permissionsMap = it) } }
        }

    fun onNewLocalSignerMethod(method: LocalSignerMethod) =
        viewModelScope.launch {
            localSignerService.processMethod(method = method)
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
                    is UiEvent.OpenEventDetails -> handleOpenEventDetails(it.eventId)
                    UiEvent.CloseEventDetails -> setState { copy(eventDetailsSessionEvent = null) }
                }
            }
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

    private fun observeSessionEventsPendingUserAction() {
        viewModelScope.launch {
            localSignerService.observeSessionEventsPendingUserAction().collect { events ->
                setState { copy(requestQueue = events) }
                Timber.tag("LocalSigner").i(events.toString())
            }
        }
    }

    private fun respondToEvents(selectedEventIds: List<String>, choice: UserChoice) =
        viewModelScope.launch {
            setState { copy(responding = true) }

            val allPendingEvents = state.value.requestQueue

            val selectedChoices = selectedEventIds.map { id ->
                SessionEventUserChoice(
                    sessionEventId = id,
                    userChoice = choice,
                )
            }

            val unselectedEventIds = allPendingEvents.map { it.eventId } - selectedEventIds.toSet()
            val unselectedChoices = unselectedEventIds.map { id ->
                SessionEventUserChoice(
                    sessionEventId = id,
                    userChoice = UserChoice.Reject,
                )
            }

            localSignerService.respondToUserActions(
                eventChoices = selectedChoices + unselectedChoices,
            )

            if (choice == UserChoice.Allow || choice == UserChoice.AlwaysAllow) {
                setEffect(
                    SideEffect.ApprovalSuccess(
                        approvedMethods = localSignerService.getMethodResponses(),
                    ),
                )
            } else {
                setEffect(SideEffect.RejectionSuccess)
            }

            setState { copy(responding = false) }
        }
}

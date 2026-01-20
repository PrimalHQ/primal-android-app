package net.primal.android.settings.connected.event.local

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.navigation.eventIdOrThrow
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onSuccess
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.domain.account.repository.SessionEventRepository
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

@HiltViewModel
class LocalEventDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionEventRepository: SessionEventRepository,
    private val permissionsRepository: PermissionsRepository,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private val eventId: String = savedStateHandle.eventIdOrThrow

    private val _state = MutableStateFlow(LocalEventDetailsContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: LocalEventDetailsContract.UiState.() -> LocalEventDetailsContract.UiState) =
        _state.getAndUpdate(reducer)

    init {
        fetchPermissionsNamingMap()
        observeSessionEvent()
    }

    private fun fetchPermissionsNamingMap() =
        viewModelScope.launch {
            permissionsRepository.getNamingMap()
                .onSuccess { setState { copy(namingMap = it) } }
        }

    private fun observeSessionEvent() =
        viewModelScope.launch {
            sessionEventRepository.observeLocalEvent(eventId = eventId).collect { sessionEvent ->
                var parsedSigned: NostrEvent? = null
                var parsedUnsigned: NostrUnsignedEvent? = null
                var rawJson: String? = null

                if (sessionEvent is SessionEvent.SignEvent) {
                    rawJson = sessionEvent.signedNostrEventJson

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
                        loading = false,
                        sessionEvent = sessionEvent,
                        parsedSignedEvent = parsedSigned,
                        parsedUnsignedEvent = parsedUnsigned,
                        rawJson = rawJson,
                        requestTypeId = sessionEvent?.requestTypeId,
                        eventNotSupported = sessionEvent == null,
                    )
                }
            }
        }
}

package net.primal.android.settings.connected.event

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
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.navigation.eventIdOrThrow
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.account.repository.SessionEventRepository
import net.primal.domain.nostr.NostrEvent

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionEventRepository: SessionEventRepository,
) : ViewModel() {

    private val eventId: String = savedStateHandle.eventIdOrThrow

    private val _state = MutableStateFlow(EventDetailsContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: EventDetailsContract.UiState.() -> EventDetailsContract.UiState) =
        _state.getAndUpdate(reducer)

    private val _effect = Channel<EventDetailsContract.SideEffect>()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: EventDetailsContract.SideEffect) = viewModelScope.launch { _effect.send(effect) }

    fun setEvent(event: EventDetailsContract.UiEvent) {
        when (event) {
            is EventDetailsContract.UiEvent.CopyToClipboard -> {
                setEffect(EventDetailsContract.SideEffect.TextCopied(label = event.label))
            }
        }
    }

    init {
        observeEvent()
    }

    private fun observeEvent() =
        viewModelScope.launch {
            sessionEventRepository.observeEvent(eventId = eventId).collect { sessionEvent ->
                if (sessionEvent is SessionEvent.SignEvent) {
                    val rawJson = sessionEvent.signedNostrEventJson
                    val nostrEvent = rawJson?.let {
                        NostrJsonEncodeDefaults.decodeFromString<NostrEvent>(it)
                    }
                    setState {
                        copy(
                            loading = false,
                            event = nostrEvent,
                            rawJson = rawJson,
                        )
                    }
                } else {
                    setState { copy(loading = false) }
                }
            }
        }
}

package net.primal.android.stream

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.navigation.naddr
import net.primal.android.stream.LiveStreamContract.StreamInfoUi
import net.primal.android.stream.LiveStreamContract.UiEvent
import net.primal.android.stream.LiveStreamContract.UiState
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV
import net.primal.domain.nostr.asATagValue
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.streams.StreamRepository
import timber.log.Timber

@HiltViewModel
class LiveStreamViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val streamRepository: StreamRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        resolveNaddr()
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.OnPlayerStateUpdate -> {
                        setState {
                            copy(
                                isPlaying = it.isPlaying ?: this.isPlaying,
                                isBuffering = it.isBuffering ?: this.isBuffering,
                                atLiveEdge = it.atLiveEdge ?: this.atLiveEdge,
                                currentTime = it.currentTime ?: this.currentTime,
                                totalDuration = it.totalDuration ?: this.totalDuration,
                            )
                        }
                    }
                    is UiEvent.OnSeekStarted -> {
                        setState { copy(isSeeking = true) }
                    }
                    is UiEvent.OnSeek -> {
                        setState { copy(isSeeking = false, currentTime = it.positionMs) }
                    }
                }
            }
        }

    private fun resolveNaddr() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            val naddr = parseAndResolveNaddr()
            if (naddr != null) {
                observeStream(naddr = naddr)
            } else {
                Timber.w("Unable to resolve naddr.")
                setState { copy(loading = false) }
            }
        }

    private suspend fun parseAndResolveNaddr(): Naddr? {
        return savedStateHandle.naddr?.let {
            Nip19TLV.parseUriAsNaddrOrNull(it)
        }
    }

    private fun observeStream(naddr: Naddr) =
        viewModelScope.launch {
            streamRepository.observeStream(aTag = naddr.asATagValue())
                .filterNotNull()
                .collect { stream ->
                    val streamingUrl = stream.streamingUrl
                    if (streamingUrl == null) {
                        setState { copy(loading = false) }
                        return@collect
                    }

                    val authorId = stream.authorId
                    profileRepository.observeProfileData(profileId = authorId).collect { profileData ->
                        val isLive = stream.isLive()
                        setState {
                            copy(
                                loading = false,
                                isLive = isLive,
                                atLiveEdge = isLive,
                                streamInfo = StreamInfoUi(
                                    title = stream.title ?: "Live Stream",
                                    streamUrl = streamingUrl,
                                    authorProfile = profileData.asProfileDetailsUi(),
                                    viewers = stream.currentParticipants ?: 0,
                                    startedAt = stream.startsAt,
                                ),
                                comment = TextFieldValue(text = streamingUrl),
                            )
                        }
                    }
                }
        }
}

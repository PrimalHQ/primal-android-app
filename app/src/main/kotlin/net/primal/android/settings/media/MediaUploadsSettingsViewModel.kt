package net.primal.android.settings.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.settings.media.MediaUploadsSettingsContract.UiEvent
import net.primal.android.settings.media.MediaUploadsSettingsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.BlossomRepository
import net.primal.core.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel
class MediaUploadsSettingsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val blossomRepository: BlossomRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        fetchSuggestedBlossomServers()
        ensureBlossomServerList()
    }

    private fun ensureBlossomServerList() = viewModelScope.launch {
        setState { copy(isLoadingBlossomServerUrls = true) }

        try {
            val userId = activeAccountStore.activeUserId()
            blossomRepository.ensureBlossomServerList(userId = userId)
            val blossoms = blossomRepository.getBlossomServers(userId = userId)

            if (blossoms.isNotEmpty()) {
                val primaryServer = blossoms.first()
                val mirrorServer = blossoms.getOrNull(1)

                setState {
                    copy(
                        blossomServerUrl = primaryServer,
                        blossomServerMirrorUrl = mirrorServer ?: blossomServerMirrorUrl,
                        blossomMirrorEnabled = mirrorServer != null
                    )
                }
            }
        } catch (error: WssException) {
            Timber.w(error)
        } finally {
            setState { copy(isLoadingBlossomServerUrls = false) }
        }
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.UpdateMediaUploadsMode -> setState { copy(mode = it.mode) }
                    is UiEvent.UpdateNewBlossomServerUrl -> setState { copy(newBlossomServerUrl = it.url) }
                    is UiEvent.ConfirmBlossomServerUrl -> confirmBlossomServerUrl(it.url)
                    is UiEvent.UpdateNewBlossomMirrorServerUrl -> setState { copy(newBlossomServerMirrorUrl = it.url) }
                    is UiEvent.ConfirmBlossomMirrorServerUrl -> confirmBlossomMirrorServerUrl(it.url)
                    is UiEvent.UpdateBlossomMirrorEnabled -> setState { copy(blossomMirrorEnabled = it.enabled) }
                    UiEvent.RestoreDefaultBlossomServer -> restoreDefaultBlossomServer()
                }
            }
        }

    private fun confirmBlossomMirrorServerUrl(url: String) {
        setState {
            copy(
                blossomServerMirrorUrl = url,
                newBlossomServerMirrorUrl = "",
                mode = MediaUploadsMode.View,
            )
        }
    }

    private suspend fun restoreDefaultBlossomServer() {
        val userId = activeAccountStore.activeUserId()
        val mirrorUrl = state.value.blossomServerMirrorUrl

        val serverList = buildList {
            add(DEFAULT_BLOSSOM_URL)
            if (mirrorUrl.isNotBlank() && mirrorUrl != DEFAULT_BLOSSOM_URL) {
                add(mirrorUrl)
            }
        }

        blossomRepository.publishBlossomServerList(
            userId = userId,
            servers = serverList
        )

        setState {
            copy(
                blossomServerUrl = DEFAULT_BLOSSOM_URL,
                newBlossomServerUrl = "",
                mode = MediaUploadsMode.View,
            )
        }
    }

    private fun fetchSuggestedBlossomServers() =
        viewModelScope.launch {
            try {
                val suggestedBlossoms = blossomRepository.fetchSuggestedBlossomList()
                setState { copy(suggestedBlossomServers = suggestedBlossoms) }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun confirmBlossomServerUrl(url: String) =
        setState {
            copy(
                blossomServerUrl = url,
                newBlossomServerUrl = "",
                mode = MediaUploadsMode.View,
            )
        }

    companion object {
        private const val DEFAULT_BLOSSOM_URL = "blossom.primal.net"
    }
}

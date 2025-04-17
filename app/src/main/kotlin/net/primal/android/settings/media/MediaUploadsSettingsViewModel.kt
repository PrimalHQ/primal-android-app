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
import net.primal.android.core.errors.UiError
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.settings.media.MediaUploadsSettingsContract.UiEvent
import net.primal.android.settings.media.MediaUploadsSettingsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.BlossomRepository
import net.primal.core.networking.sockets.errors.WssException
import net.primal.domain.nostr.cryptography.SignatureException
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

    private fun ensureBlossomServerList() =
        viewModelScope.launch {
            setState { copy(isLoadingBlossomServerUrls = true) }

            try {
                val userId = activeAccountStore.activeUserId()
                val blossoms = blossomRepository.ensureBlossomServerList(userId = userId)

                if (blossoms.isNotEmpty()) {
                    val primaryServer = blossoms.first()
                    val mirrorServers = blossoms.drop(1)

                    setState {
                        copy(
                            blossomServerUrl = primaryServer,
                            mirrorBlossomServerUrls = mirrorServers,
                            blossomMirrorEnabled = mirrorServers.isNotEmpty(),
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
                    is UiEvent.UpdateBlossomMirrorEnabled -> updateBlossomMirrorEnabled(it.enabled)
                    UiEvent.RestoreDefaultBlossomServer -> restoreDefaultBlossomServer()
                }
            }
        }

    private fun confirmBlossomServerUrl(url: String) {
        val mirrorUrls = state.value.mirrorBlossomServerUrls

        val serverList = buildList {
            add(url)
            addAll(mirrorUrls.filterNot { it == url })
        }

        updateBlossomServers(serverList) {
            copy(
                blossomServerUrl = url,
                newBlossomServerUrl = "",
                mode = MediaUploadsMode.View,
            )
        }
    }

    private fun updateBlossomMirrorEnabled(blossomMirrorEnabled: Boolean) {
        if (blossomMirrorEnabled) {
            setState {
                copy(
                    blossomMirrorEnabled = true,
                    mirrorBlossomServerUrls = emptyList(),
                    newBlossomServerMirrorUrl = "",
                )
            }
            return
        }

        val primaryUrl = state.value.blossomServerUrl
        val serverList = listOf(primaryUrl)

        updateBlossomServers(serverList) {
            copy(blossomMirrorEnabled = false)
        }
    }

    private fun confirmBlossomMirrorServerUrl(url: String) {
        val primaryUrl = state.value.blossomServerUrl
        val mirrors = state.value.mirrorBlossomServerUrls

        val serverList = buildList {
            add(primaryUrl)
            addAll(mirrors.filterNot { it == primaryUrl })
            if (url.isNotBlank() && url != primaryUrl && url !in mirrors) {
                add(url)
            }
        }

        updateBlossomServers(serverList) {
            copy(
                mirrorBlossomServerUrls = serverList.drop(1),
                newBlossomServerMirrorUrl = "",
                mode = MediaUploadsMode.View,
            )
        }
    }

    private fun restoreDefaultBlossomServer() {
        val serverUrls = buildList {
            add(DEFAULT_BLOSSOM_URL)
            addAll(state.value.mirrorBlossomServerUrls.filterNot { it == DEFAULT_BLOSSOM_URL })
        }

        updateBlossomServers(serverUrls) {
            copy(
                blossomServerUrl = DEFAULT_BLOSSOM_URL,
                newBlossomServerUrl = "",
                mode = MediaUploadsMode.View,
            )
        }
    }

    private fun updateBlossomServers(servers: List<String>, onSuccess: UiState.() -> UiState) =
        viewModelScope.launch {
            try {
                val userId = activeAccountStore.activeUserId()
                blossomRepository.publishBlossomServerList(userId = userId, servers = servers)
                setState(onSuccess)
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToUpdateBlossomServer(error)) }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToUpdateBlossomServer(error)) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToUpdateBlossomServer(error)) }
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

    companion object {
        private const val DEFAULT_BLOSSOM_URL = "blossom.primal.net"
    }
}

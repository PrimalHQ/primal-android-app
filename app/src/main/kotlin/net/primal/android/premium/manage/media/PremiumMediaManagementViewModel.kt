package net.primal.android.premium.manage.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.nostr.notary.exceptions.SignException
import net.primal.android.premium.manage.media.PremiumMediaManagementContract.SideEffect
import net.primal.android.premium.manage.media.PremiumMediaManagementContract.UiEvent
import net.primal.android.premium.manage.media.PremiumMediaManagementContract.UiState
import net.primal.android.premium.manage.media.repository.MediaManagementRepository
import net.primal.android.premium.manage.media.ui.MediaType
import net.primal.android.premium.manage.media.ui.MediaUiItem
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel
class PremiumMediaManagementViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val mediaManagementRepository: MediaManagementRepository,
    private val premiumRepository: PremiumRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    init {
        observeActiveAccount()
        observeEvents()
        fetchMediaStats()
        fetchMediaUploads()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.DeleteMedia -> deleteMedia(mediaUrl = it.mediaUrl)
                }
            }
        }
    }

    private fun observeActiveAccount() {
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        usedStorageInBytes = it.premiumMembership?.usedStorageInBytes,
                        maxStorageInBytes = it.premiumMembership?.maxStorageInBytes,
                    )
                }
            }
        }
    }

    private fun fetchMediaUploads() {
        viewModelScope.launch {
            try {
                val mediaUploads = mediaManagementRepository.fetchMediaUploads(
                    userId = activeAccountStore.activeUserId(),
                ) ?: emptyList()

                val uploads = mediaUploads.map { mediaInfo ->
                    val cdnVariant = mediaInfo.cdnResource?.variants?.minByOrNull { it.width }
                    MediaUiItem(
                        mediaId = mediaInfo.url,
                        thumbnailUrl = cdnVariant?.mediaUrl,
                        mediaUrl = mediaInfo.url,
                        sizeInBytes = mediaInfo.sizeInBytes,
                        type = when {
                            mediaInfo.mimetype?.contains("image") == true -> MediaType.Image
                            mediaInfo.mimetype?.contains("video") == true -> MediaType.Video
                            else -> MediaType.Other
                        },
                        createdAt = mediaInfo.createdAt?.let(Instant::ofEpochSecond),
                    )
                }

                setState { copy(mediaItems = uploads) }
            } catch (error: SignException) {
                Timber.e(error)
            } catch (error: WssException) {
                Timber.e(error)
            }
        }
    }

    private fun fetchMediaStats() {
        viewModelScope.launch {
            setState { copy(calculating = true) }
            try {
                val stats = mediaManagementRepository.fetchMediaStats(userId = activeAccountStore.activeUserId())
                setState {
                    copy(
                        imagesInBytes = stats.imagesInBytes,
                        videosInBytes = stats.videosInBytes,
                        otherInBytes = stats.otherFilesInBytes,
                    )
                }
            } catch (error: SignException) {
                Timber.e(error)
            } catch (error: WssException) {
                Timber.e(error)
            } finally {
                setState { copy(calculating = false) }
            }
        }
    }

    private fun deleteMedia(mediaUrl: String) {
        val userId = activeAccountStore.activeUserId()
        viewModelScope.launch {
            try {
                mediaManagementRepository.deleteMedia(userId = userId, mediaUrl = mediaUrl)
                setState {
                    copy(
                        mediaItems = mediaItems.toMutableList().apply {
                            removeIf { it.mediaUrl == mediaUrl }
                        },
                    )
                }
                setEffect(SideEffect.MediaDeleted(mediaUrl = mediaUrl))

                fetchMediaStats()
                premiumRepository.fetchMembershipStatus(userId = userId)
            } catch (error: SignException) {
                Timber.e(error)
            } catch (error: WssException) {
                Timber.e(error)
            }
        }
    }
}

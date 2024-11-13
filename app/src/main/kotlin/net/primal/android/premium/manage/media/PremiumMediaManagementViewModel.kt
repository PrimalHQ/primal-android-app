package net.primal.android.premium.manage.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.manage.media.PremiumMediaManagementContract.UiState
import net.primal.android.premium.manage.media.repository.MediaManagementRepository
import net.primal.android.premium.manage.media.ui.MediaType
import net.primal.android.premium.manage.media.ui.MediaUiItem
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel
class PremiumMediaManagementViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val mediaManagementRepository: MediaManagementRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        observeActiveAccount()
        fetchMediaStats()
        fetchMediaUploads()
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
            } catch (error: WssException) {
                Timber.e(error)
            } finally {
                setState { copy(calculating = false) }
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
}

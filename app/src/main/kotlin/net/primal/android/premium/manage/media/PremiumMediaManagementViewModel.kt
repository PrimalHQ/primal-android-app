package net.primal.android.premium.manage.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.premium.manage.media.PremiumMediaManagementContract.UiState
import net.primal.android.premium.manage.media.model.MediaType
import net.primal.android.premium.manage.media.model.MediaUiItem
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class PremiumMediaManagementViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        setMaxAndUsedStorage()
        fetchMediaBreakdown()
    }

    private fun setMaxAndUsedStorage() =
        viewModelScope.launch {
            val premiumMembership = activeAccountStore.activeUserAccount().premiumMembership
            setState {
                copy(
                    usedStorageInBytes = premiumMembership?.usedStorageInBytes,
                    maxStorageInBytes = premiumMembership?.maxStorageInBytes,
                )
            }
        }

    private fun fetchMediaBreakdown() =
        viewModelScope.launch {
            val imgUrl = "https://images.stockcake.com/public/9/e/0/9e0955ab-0177-" +
                "48a2-a346-1525da173e28_medium/verdant-grass-field-stockcake.jpg"
            delay(1.seconds)
            setState {
                copy(
                    calculating = false,
                    imagesInBytes = 35_791_394_133,
                    videosInBytes = 17_791_394_133,
                    otherInBytes = 12_791_394_133,
                    mediaItems = listOf(
                        MediaUiItem(
                            mediaId = "asdf",
                            thumbnailUrl = imgUrl,
                            mediaUrl = imgUrl,
                            sizeInBytes = 1_000_000,
                            type = MediaType.Image,
                            date = Instant.now(),
                        ),
                        MediaUiItem(
                            mediaId = "asdf1",
                            thumbnailUrl = imgUrl,
                            mediaUrl = imgUrl,
                            sizeInBytes = 1_000_000,
                            type = MediaType.Video,
                            date = Instant.now(),
                        ),
                        MediaUiItem(
                            mediaId = "asdf2",
                            thumbnailUrl = imgUrl,
                            mediaUrl = imgUrl,
                            sizeInBytes = 2_600_000,
                            type = MediaType.Image,
                            date = Instant.now(),
                        ),
                        MediaUiItem(
                            mediaId = "asdf3",
                            thumbnailUrl = imgUrl,
                            mediaUrl = imgUrl,
                            sizeInBytes = 3_000_000,
                            type = MediaType.Video,
                            date = Instant.now(),
                        ),
                        MediaUiItem(
                            mediaId = "asdf4",
                            thumbnailUrl = imgUrl,
                            mediaUrl = imgUrl,
                            sizeInBytes = 1_000_000,
                            type = MediaType.Image,
                            date = Instant.now(),
                        ),
                    ),
                )
            }
        }
}

package net.primal.android.premium.manage.media

import net.primal.android.premium.manage.media.ui.MediaUiItem

interface PremiumMediaManagementContract {
    data class UiState(
        val usedStorageInBytes: Long? = null,
        val maxStorageInBytes: Long? = null,
        val imagesInBytes: Long? = null,
        val videosInBytes: Long? = null,
        val otherInBytes: Long? = null,
        val mediaItems: List<MediaUiItem> = emptyList(),
        val calculating: Boolean = true,
    )

    sealed class UiEvent {
        data class DeleteMedia(val mediaUrl: String) : UiEvent()
    }

    sealed class SideEffect {
        data class MediaDeleted(val mediaUrl: String) : SideEffect()
    }
}

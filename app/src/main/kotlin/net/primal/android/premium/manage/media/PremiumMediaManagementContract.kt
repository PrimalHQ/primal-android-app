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
}

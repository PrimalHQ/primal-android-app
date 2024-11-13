package net.primal.android.premium.manage.media.api

import net.primal.android.premium.manage.media.api.model.MediaStorageStats
import net.primal.android.premium.manage.media.api.model.MediaUploadsResponse

interface MediaManagementApi {

    suspend fun getMediaStats(userId: String): MediaStorageStats

    suspend fun getMediaUploads(userId: String): MediaUploadsResponse

    suspend fun deleteMedia(userId: String)
}

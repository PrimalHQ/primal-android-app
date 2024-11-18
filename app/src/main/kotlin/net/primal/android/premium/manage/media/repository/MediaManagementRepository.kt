package net.primal.android.premium.manage.media.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.asMapByKey
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.takeContentOrNull
import net.primal.android.premium.manage.media.api.MediaManagementApi
import net.primal.android.premium.manage.media.api.model.MediaUploadInfo
import net.primal.android.premium.manage.media.domain.MediaUpload

class MediaManagementRepository @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val mediaManagementApi: MediaManagementApi,
) {

    suspend fun fetchMediaStats(userId: String) =
        withContext(dispatchers.io()) {
            mediaManagementApi.getMediaStats(userId = userId)
        }

    suspend fun fetchMediaUploads(userId: String) =
        withContext(dispatchers.io()) {
            val response = mediaManagementApi.getMediaUploads(userId = userId)
            val uploadInfoList = response.uploadInfo.takeContentOrNull<List<MediaUploadInfo>>()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            uploadInfoList?.map {
                MediaUpload(
                    url = it.url,
                    mimetype = it.mimetype,
                    cdnResource = cdnResources[it.url],
                    createdAt = it.createdAt,
                    sizeInBytes = it.sizeInBytes,
                )
            }
        }

    suspend fun deleteMedia(userId: String, mediaUrl: String) =
        withContext(dispatchers.io()) {
            mediaManagementApi.deleteMedia(userId = userId, mediaUrl = mediaUrl)
        }
}

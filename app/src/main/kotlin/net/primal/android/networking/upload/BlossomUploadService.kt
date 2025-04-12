package net.primal.android.networking.upload

import android.content.ContentResolver
import android.net.Uri
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.BuildConfig
import net.primal.android.networking.UserAgentProvider
import net.primal.core.networking.blossom.BlossomUploader
import net.primal.core.networking.blossom.UnsuccessfulBlossomUpload
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.upload.UploadResult
import okio.buffer
import okio.source

class BlossomUploadService @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val contentResolver: ContentResolver,
    private val blossomUploader: BlossomUploader,
) {
    companion object {
        fun generateRandomUploadId(): String {
            val uploadFriendlyVersionName = BuildConfig.VERSION_NAME.replace(".", "_")
            return "${UUID.randomUUID()}-${UserAgentProvider.APP_NAME}-$uploadFriendlyVersionName"
        }
    }

    @Throws(UnsuccessfulBlossomUpload::class)
    suspend fun upload(
        uri: Uri,
        userId: String,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult =
        withContext(dispatchers.io()) {
            val descriptor = blossomUploader.uploadBlob(
                userId = userId,
                blossomUrl = "https://blossom.primal.net",
                inputStream = {
                    contentResolver.openInputStream(uri)?.source()?.buffer()
                        ?: throw IOException("Unable to open input stream.")
                },
                onProgress = onProgress,
            )

            UploadResult(
                remoteUrl = descriptor.url,
                originalFileSize = descriptor.sizeInBytes,
                originalHash = descriptor.sha256,
            )
        }

    suspend fun cancelOrDelete(userId: String, uploadId: String) = Unit
}

package net.primal.android.gifpicker

import android.net.Uri
import io.github.aakira.napier.Napier
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import net.primal.core.networking.blossom.AndroidPrimalBlossomUploadService
import net.primal.core.networking.blossom.UploadResult
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.remote.api.klipy.KlipyApi

@Singleton
class GifBlossomUploader @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val klipyApi: KlipyApi,
    private val primalUploadService: AndroidPrimalBlossomUploadService,
) {

    suspend fun uploadToBlossom(gifUrl: String, userId: String): UploadResult {
        val tempFile = downloadGifToTempFile(gifUrl)
            ?: return UploadResult.Failed(
                error = net.primal.core.networking.blossom.BlossomException("Failed to download GIF."),
            )

        val result = primalUploadService.upload(uri = Uri.fromFile(tempFile), userId = userId)
        tempFile.delete()
        return result
    }

    private suspend fun downloadGifToTempFile(url: String): File? =
        withContext(dispatchers.io()) {
            runCatching {
                val bytes = klipyApi.downloadGifBytes(url)
                val file = File.createTempFile("gif", ".gif")
                file.writeBytes(bytes)
                file
            }.onFailure { error ->
                Napier.w(throwable = error) { "Failed to download GIF." }
            }.getOrNull()
        }
}

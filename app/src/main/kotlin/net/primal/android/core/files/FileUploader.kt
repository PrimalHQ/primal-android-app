package net.primal.android.core.files

import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.core.files.model.UploadImageRequest
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.networking.di.PrimalUploadApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary
import timber.log.Timber

@Singleton
class FileUploader @Inject constructor(
    private val contentResolver: ContentResolver,
    private val nostrNotary: NostrNotary,
    @PrimalUploadApiClient private val primalUploadClient: PrimalApiClient,
) {

    @Throws(UnsuccessfulFileUpload::class)
    suspend fun uploadFile(userId: String, uri: Uri): String {
        val imageBytes = uri.readBytesSafely()
        val imageAsBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        val uploadImageNostrEvent = nostrNotary.signImageUploadNostrEvent(
            userId = userId,
            base64Content = "data:image/svg+xml;base64,$imageAsBase64",
        )

        val queryResult = try {
            primalUploadClient.query(
                message = PrimalCacheFilter(
                    primalVerb = PrimalVerb.UPLOAD,
                    optionsJson = NostrJsonEncodeDefaults.encodeToString(
                        UploadImageRequest(uploadImageEvent = uploadImageNostrEvent),
                    ),
                ),
            )
        } catch (error: WssException) {
            Timber.w(error)
            throw UnsuccessfulFileUpload(cause = error)
        } catch (error: IOException) {
            Timber.w(error)
            throw UnsuccessfulFileUpload(cause = error)
        } catch (error: UnknownHostException) {
            Timber.w(error)
            throw UnsuccessfulFileUpload(cause = error)
        }

        return queryResult
            .findPrimalEvent(NostrEventKind.PrimalImageUploadResponse)
            ?.content ?: throw UnsuccessfulFileUpload(cause = null)
    }

    private suspend fun Uri.readBytesSafely(): ByteArray? =
        withContext(Dispatchers.IO) {
            contentResolver.openInputStream(this@readBytesSafely)?.use { it.readBytes() }
        }
}

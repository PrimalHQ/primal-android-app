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
import kotlinx.serialization.json.Json
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.core.files.model.UploadImageRequest
import net.primal.android.networking.di.PrimalUploadApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary

@Singleton
class FileUploader @Inject constructor(
    private val contentResolver: ContentResolver,
    private val nostrNotary: NostrNotary,
    @PrimalUploadApiClient private val primalUploadClient: PrimalApiClient,
) {

    private val uploadJsonSerializer = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

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
                    optionsJson = uploadJsonSerializer.encodeToString(
                        UploadImageRequest(uploadImageEvent = uploadImageNostrEvent),
                    ),
                ),
            )
        } catch (error: WssException) {
            throw UnsuccessfulFileUpload(cause = error)
        } catch (error: IOException) {
            throw UnsuccessfulFileUpload(cause = error)
        } catch (error: UnknownHostException) {
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

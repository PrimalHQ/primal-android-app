package net.primal.core.networking.blossom

import android.content.ContentResolver
import android.net.Uri
import java.io.IOException
import net.primal.core.utils.coroutines.DispatcherProviderFactory
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import okio.BufferedSource
import okio.buffer
import okio.source

class AndroidPrimalBlossomUploadService(
    blossomResolver: BlossomServerListProvider,
    signatureHandler: NostrEventSignatureHandler,
    private val contentResolver: ContentResolver,
) {

    private val uploadService by lazy {
        PrimalUploadService(
            dispatchers = DispatcherProviderFactory.create(),
            blossomResolver = blossomResolver,
            signatureHandler = signatureHandler,
        )
    }

    suspend fun upload(
        uri: Uri,
        userId: String,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult {
        return uploadService.upload(
            userId = userId,
            openBufferedSource = { uri.openBufferedSource() },
            onProgress = onProgress,
        )
    }

    suspend fun upload(
        uri: Uri,
        userId: String,
        onSignRequested: (NostrUnsignedEvent) -> NostrEvent,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult {
        return uploadService.upload(
            userId = userId,
            openBufferedSource = { uri.openBufferedSource() },
            onProgress = onProgress,
            onSignRequested = onSignRequested,
        )
    }

    private fun Uri.openBufferedSource(): BufferedSource {
        return contentResolver.openInputStream(this)?.source()?.buffer()
            ?: throw IOException("Unable to open input stream.")
    }
}

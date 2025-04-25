package net.primal.core.networking.blossom

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import net.primal.core.utils.coroutines.DispatcherProviderFactory
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer

class IosPrimalBlossomUploadService(
    blossomResolver: BlossomServerListProvider,
    signatureHandler: NostrEventSignatureHandler? = null,
) {

    private val uploadService by lazy {
        Napier.base(DebugAntilog())
        PrimalUploadService(
            dispatchers = DispatcherProviderFactory.create(),
            blossomResolver = blossomResolver,
            signatureHandler = signatureHandler,
        )
    }

    suspend fun upload(
        path: String,
        userId: String,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult {
        return uploadService.upload(
            userId = userId,
            openBufferedSource = { path.openBufferedSourceFromPath() },
            onProgress = onProgress,
        )
    }

    suspend fun upload(
        path: String,
        userId: String,
        onSignRequested: (NostrUnsignedEvent) -> NostrEvent,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult {
        return uploadService.upload(
            userId = userId,
            openBufferedSource = { path.openBufferedSourceFromPath() },
            onProgress = onProgress,
            onSignRequested = onSignRequested,
        )
    }

    private fun String.openBufferedSourceFromPath(): BufferedSource {
        return FileSystem.SYSTEM.source(this.toPath()).buffer()
    }
}

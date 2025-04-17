package net.primal.android.networking.di

import android.content.ContentResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.upload.AndroidBlossomServerListProvider
import net.primal.core.networking.blossom.AndroidPrimalBlossomUploadService
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler

@Module
@InstallIn(SingletonComponent::class)
class UploadModule {

    @Provides
    fun providesAndroidPrimalBlossomUploadService(
        contentResolver: ContentResolver,
        signatureHandler: NostrEventSignatureHandler,
        blossomServerListProvider: AndroidBlossomServerListProvider,
    ): AndroidPrimalBlossomUploadService {
        return AndroidPrimalBlossomUploadService(
            contentResolver = contentResolver,
            blossomResolver = blossomServerListProvider,
            signatureHandler = signatureHandler,
        )
    }
}

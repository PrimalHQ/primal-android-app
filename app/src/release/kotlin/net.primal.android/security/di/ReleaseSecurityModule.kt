package net.primal.android.security.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.BuildConfig
import net.primal.android.security.Encryption
import net.primal.android.security.AESEncryption

@Module
@InstallIn(SingletonComponent::class)
object ReleaseSecurityModule {

    @Provides
    fun provideEncryption(): Encryption = AESEncryption(
        keyAlias = BuildConfig.LOCAL_STORAGE_KEY_ALIAS,
    )

}

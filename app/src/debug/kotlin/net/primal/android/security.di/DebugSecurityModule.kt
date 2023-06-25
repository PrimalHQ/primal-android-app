package net.primal.android.security.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.security.Encryption
import net.primal.android.security.NoEncryption

@Module
@InstallIn(SingletonComponent::class)
object DebugSecurityModule {

    @Provides
    fun provideEncryption(): Encryption = NoEncryption()

}

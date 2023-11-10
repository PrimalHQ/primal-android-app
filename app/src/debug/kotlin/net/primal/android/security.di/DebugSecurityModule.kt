package net.primal.android.security.di

import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
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

    @Provides
    fun provideDatabaseOpenHelper(): SupportSQLiteOpenHelper.Factory {
        return FrameworkSQLiteOpenHelperFactory()
    }

}

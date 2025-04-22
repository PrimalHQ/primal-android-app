package net.primal.android.security.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.sqlite.db.SupportSQLiteOpenHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.primal.android.BuildConfig
import net.primal.android.core.serialization.datastore.StringSerializer
import net.primal.android.security.AESEncryption
import net.primal.android.security.Encryption
import net.primal.android.security.PrimalDatabasePasswordProvider
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Module
@InstallIn(SingletonComponent::class)
object ReleaseSecurityModule {

    @Provides
    fun provideEncryption(): Encryption =
        AESEncryption(
            keyAlias = BuildConfig.LOCAL_STORAGE_KEY_ALIAS,
        )

    @Provides
    fun databasePasswordStore(@ApplicationContext context: Context, encryption: Encryption): DataStore<String> =
        DataStoreFactory.create(
            produceFile = { context.dataStoreFile("db_key.txt") },
            serializer = StringSerializer(encryption = encryption),
        )

    @Provides
    fun provideDatabaseOpenHelper(
        databasePasswordProvider: PrimalDatabasePasswordProvider,
    ): SupportSQLiteOpenHelper.Factory {
        return SupportOpenHelperFactory(
            databasePasswordProvider.providePassword().toByteArray(),
        )
    }
}

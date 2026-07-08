package net.primal.android.migration.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import net.primal.android.core.serialization.datastore.StringSerializer
import net.primal.android.migration.CURRENT_APP_VERSION

@Module
@InstallIn(SingletonComponent::class)
object AppMigrationModule {

    @Provides
    @Singleton
    @AppMigrationVersionDataStore
    fun appMigrationVersionDataStore(@ApplicationContext context: Context): DataStore<String> =
        DataStoreFactory.create(
            produceFile = { context.dataStoreFile("app_migration_version.txt") },
            serializer = StringSerializer(),
        )

    @Provides
    @CurrentAppVersion
    fun currentAppVersion(): Int = CURRENT_APP_VERSION

    // Register migrations here with @Provides @IntoSet as the chain grows.
}

@Qualifier
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class AppMigrationVersionDataStore

@Qualifier
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class CurrentAppVersion

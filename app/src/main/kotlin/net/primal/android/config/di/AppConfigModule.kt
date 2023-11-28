package net.primal.android.config.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.android.config.AppConfigProvider
import net.primal.android.config.api.WellKnownApi
import net.primal.android.config.domain.AppConfig
import net.primal.android.config.dynamic.DynamicConfigProvider
import net.primal.android.config.store.AppConfigDataStore
import net.primal.android.config.store.AppConfigSerialization
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.security.Encryption
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {

    @Provides
    @Singleton
    fun wellKnownApi(retrofit: Retrofit): WellKnownApi = retrofit.create()

    @Provides
    @Singleton
    fun provideAppConfigStore(@ApplicationContext context: Context, encryption: Encryption): DataStore<AppConfig> =
        DataStoreFactory.create(
            produceFile = { context.dataStoreFile("app_config.json") },
            serializer = AppConfigSerialization(encryption = encryption),
        )

    @Provides
    fun appConfigProvider(
        appConfigDataStore: AppConfigDataStore,
        dispatcherProvider: CoroutineDispatcherProvider,
    ): AppConfigProvider =
        DynamicConfigProvider(
            appConfigStore = appConfigDataStore,
            dispatcherProvider = dispatcherProvider,
        )
}

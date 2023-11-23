package net.primal.android.theme.active.di

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

@Module
@InstallIn(SingletonComponent::class)
object ThemeModule {

    @Provides
    @Singleton
    @ActiveThemeDataStore
    fun activeThemeDataStore(@ApplicationContext context: Context): DataStore<String> =
        DataStoreFactory.create(
            produceFile = { context.dataStoreFile("active_theme.txt") },
            serializer = StringSerializer(),
        )
}

@Qualifier
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class ActiveThemeDataStore

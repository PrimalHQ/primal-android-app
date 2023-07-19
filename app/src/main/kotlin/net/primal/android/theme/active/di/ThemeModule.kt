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
import net.primal.android.serialization.StringSerializer

@Module
@InstallIn(SingletonComponent::class)
object ThemeModule {

    private const val DATA_STORE_ACTIVE_THEME_FILE_NAME = "activeTheme"

    @Provides
    fun activeThemeDataStore(
        @ApplicationContext context: Context,
    ): DataStore<String> = DataStoreFactory.create(
        produceFile = { context.dataStoreFile(DATA_STORE_ACTIVE_THEME_FILE_NAME) },
        serializer = StringSerializer(),
    )
}

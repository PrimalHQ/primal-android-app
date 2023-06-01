package net.primal.android.theme.active.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.InputStream
import java.io.OutputStream

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


class StringSerializer : Serializer<String> {

    override val defaultValue: String = ""

    override suspend fun readFrom(input: InputStream): String {
        return String(input.readBytes())
    }

    override suspend fun writeTo(t: String, output: OutputStream) {
        output.use {
            it.write(t.toByteArray())
        }
    }
}

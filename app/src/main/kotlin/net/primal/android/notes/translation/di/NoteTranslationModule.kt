package net.primal.android.notes.translation.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.android.notes.translation.NoteTranslationEngine
import net.primal.android.notes.translation.device.AndroidOnDeviceNoteTranslator
import net.primal.android.notes.translation.device.OnDeviceNoteTranslator
import net.primal.android.notes.translation.remote.UserOwnedTranslationClient
import net.primal.core.utils.coroutines.DispatcherProvider
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NoteTranslationModule {

    @Provides
    @Singleton
    fun provideUserOwnedTranslationClient(okHttpClient: OkHttpClient): UserOwnedTranslationClient =
        UserOwnedTranslationClient(httpClient = okHttpClient)

    @Provides
    @Singleton
    fun provideOnDeviceNoteTranslator(
        @ApplicationContext context: Context,
    ): OnDeviceNoteTranslator = AndroidOnDeviceNoteTranslator(context)

    @Provides
    @Singleton
    fun provideNoteTranslationEngine(
        remoteClient: UserOwnedTranslationClient,
        onDeviceTranslator: OnDeviceNoteTranslator,
        dispatcherProvider: DispatcherProvider,
    ): NoteTranslationEngine =
        NoteTranslationEngine(
            remoteClient = remoteClient,
            onDeviceTranslator = onDeviceTranslator,
            dispatcherProvider = dispatcherProvider,
        )
}

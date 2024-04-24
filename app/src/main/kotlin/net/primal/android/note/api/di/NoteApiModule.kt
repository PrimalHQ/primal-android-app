package net.primal.android.note.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.note.api.NoteApi
import net.primal.android.note.api.NoteApiImpl

@Module
@InstallIn(SingletonComponent::class)
object NoteApiModule {
    @Provides
    fun provideNoteApi(@PrimalCacheApiClient primalApiClient: PrimalApiClient): NoteApi =
        NoteApiImpl(
            primalApiClient = primalApiClient,
        )
}

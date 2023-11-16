package net.primal.android.core.di

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    /* Logging */
    @Provides
    @ElementsIntoSet
    fun emptyLoggersSet(): Set<Timber.Tree> = emptySet()

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver = context.contentResolver
}

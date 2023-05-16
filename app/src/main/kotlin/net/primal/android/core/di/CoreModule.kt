package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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


}

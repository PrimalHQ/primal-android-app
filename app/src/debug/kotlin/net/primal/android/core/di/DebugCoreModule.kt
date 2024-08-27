package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
object DebugCoreModule {

    @Provides
    @IntoSet
    fun timberLogger(): Timber.Tree = Timber.DebugTree()
}

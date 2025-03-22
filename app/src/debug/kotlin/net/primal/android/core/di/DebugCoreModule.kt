package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
object DebugCoreModule {

    @Provides
    @IntoSet
    fun timberLogger(): Timber.Tree = Timber.DebugTree()

    @Provides
    @IntoSet
    fun antilog(): Antilog = DebugAntilog()
}

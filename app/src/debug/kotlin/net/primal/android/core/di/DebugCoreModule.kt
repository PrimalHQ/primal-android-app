package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import net.primal.android.core.logging.AppLogController
import net.primal.android.core.logging.NoOpAppLogController
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

    @Provides
    fun appLogController(noOpController: NoOpAppLogController): AppLogController = noOpController
}

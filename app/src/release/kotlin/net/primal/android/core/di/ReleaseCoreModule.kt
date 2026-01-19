package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.github.aakira.napier.Antilog
import net.primal.android.core.logging.AppLogController
import net.primal.android.core.logging.RecordingAntilog

@Module
@InstallIn(SingletonComponent::class)
object ReleaseCoreModule {

//    @Provides
//    @IntoSet
//    fun timberLogger(): Timber.Tree = Timber.DebugTree()

    @Provides
    @IntoSet
    fun antilog(recordingAntilog: RecordingAntilog): Antilog = recordingAntilog

    @Provides
    fun appLogController(recordingAntilog: RecordingAntilog): AppLogController = recordingAntilog
}

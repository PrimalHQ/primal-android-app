package net.primal.android.core.di

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.LoadControl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoadControlModule {

    private object PlaybackConstants {
        const val MIN_BUFFER_MSEC = 30_000

        const val MAX_BUFFER_MSEC = 120_000

        const val BUFFER_FOR_PLAYBACK_MSEC = 5_000

        const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MSEC = 5_000
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideLoadControl(): LoadControl {
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                PlaybackConstants.MIN_BUFFER_MSEC,
                PlaybackConstants.MAX_BUFFER_MSEC,
                PlaybackConstants.BUFFER_FOR_PLAYBACK_MSEC,
                PlaybackConstants.BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MSEC,
            )
            .build()
    }
}

package net.primal.android.core.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import javax.inject.Inject
import net.primal.android.core.service.PlayerManager
import net.primal.android.stream.player.LIVE_STREAM_MANIFEST_MIN_RETRY_COUNT

class AospPlayerManager @Inject constructor() : PlayerManager {

    @OptIn(UnstableApi::class)
    override fun createPlayer(context: Context): Player {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        val loadErrorHandlingPolicy =
            DefaultLoadErrorHandlingPolicy(LIVE_STREAM_MANIFEST_MIN_RETRY_COUNT)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)

        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }

    override fun cleanup() {
    }
}

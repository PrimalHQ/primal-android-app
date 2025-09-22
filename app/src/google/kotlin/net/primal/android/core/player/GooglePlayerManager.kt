package net.primal.android.core.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import net.primal.android.core.service.PlayerManager
import net.primal.android.networking.UserAgentProvider
import net.primal.android.stream.player.LIVE_STREAM_MANIFEST_MIN_RETRY_COUNT
import net.primal.core.utils.AndroidBuildConfig
import org.chromium.net.CronetEngine
import timber.log.Timber

@OptIn(UnstableApi::class)
class GooglePlayerManager @Inject constructor() : PlayerManager {

    private var cronetEngine: CronetEngine? = null
    private var cronetExecutor: ExecutorService? = null

    override fun createPlayer(context: Context): Player {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        val analyticsListener = PrimalStreamAnalyticsListener()

        val loadErrorHandlingPolicy =
            DefaultLoadErrorHandlingPolicy(LIVE_STREAM_MANIFEST_MIN_RETRY_COUNT)

        val playerBuilder = ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)

        cronetEngine = runCatching {
            CronetEngine.Builder(context)
                .enableHttp2(true)
                .enableQuic(true)
                .enableBrotli(true)
                .setUserAgent("${UserAgentProvider.APP_NAME}/${AndroidBuildConfig.APP_VERSION}")
                .build()
        }.onSuccess { engine ->
            val executor = Executors.newSingleThreadExecutor()
            this.cronetExecutor = executor

            val dataSourceFactory = CronetDataSource.Factory(engine, executor)
            val mediaSourceFactory = DefaultMediaSourceFactory(context)
                .setDataSourceFactory(dataSourceFactory)
                .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
            playerBuilder.setMediaSourceFactory(mediaSourceFactory)

            val version = runCatching { engine.versionString }.getOrElse { "unknown" }
            analyticsListener.setCronetInfo(version)
        }.onFailure {
            Timber.w(it, "Cronet is not available. Using default HTTP stack for media playback.")
            val mediaSourceFactory = DefaultMediaSourceFactory(context)
                .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
            playerBuilder.setMediaSourceFactory(mediaSourceFactory)
        }.getOrNull()

        val player = playerBuilder.build()
        player.addAnalyticsListener(analyticsListener)

        return player
    }

    override fun cleanup() {
        cronetEngine?.shutdown()
        cronetExecutor?.shutdownNow()
        cronetEngine = null
        cronetExecutor = null
    }
}

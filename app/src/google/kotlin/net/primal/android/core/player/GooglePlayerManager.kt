package net.primal.android.core.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import net.primal.android.core.di.StreamVideoCache
import net.primal.android.core.service.PlayerManager
import net.primal.android.core.service.PrimalCacheKeyFactory
import net.primal.android.networking.UserAgentProvider
import net.primal.android.stream.player.LIVE_STREAM_MANIFEST_MIN_RETRY_COUNT
import net.primal.android.stream.player.SEEK_BACK_MS
import net.primal.android.stream.player.SEEK_FORWARD_MS
import net.primal.core.utils.AndroidBuildConfig
import org.chromium.net.CronetEngine
import timber.log.Timber

@OptIn(UnstableApi::class)
class GooglePlayerManager @Inject constructor(
    private val loadControl: LoadControl,
    @StreamVideoCache private val simpleCache: SimpleCache,
) : PlayerManager {

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
            .setLoadControl(loadControl)
            .setSeekBackIncrementMs(SEEK_BACK_MS)
            .setSeekForwardIncrementMs(SEEK_FORWARD_MS)

        val upstreamDataSourceFactory: DataSource.Factory = runCatching {
            val engine = CronetEngine.Builder(context)
                .enableHttp2(true)
                .enableQuic(true)
                .enableBrotli(true)
                .setUserAgent("${UserAgentProvider.APP_NAME}/${AndroidBuildConfig.APP_VERSION}")
                .build()
            val executor = Executors.newSingleThreadExecutor()
            engine to executor
        }.onSuccess { (engine, executor) ->
            cronetEngine = engine
            cronetExecutor = executor
            val version = runCatching { engine.versionString }.getOrElse { "unknown" }
            analyticsListener.setCronetInfo(version)
        }.map { (engine, executor) ->
            CronetDataSource.Factory(engine, executor)
        }.onFailure {
            Timber.w(it, "Cronet is not available. Using default HTTP stack for media playback.")
        }.getOrNull() ?: DefaultDataSource.Factory(context)

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setCacheKeyFactory(PrimalCacheKeyFactory)
            .setUpstreamDataSourceFactory(upstreamDataSourceFactory)
            .setFlags(
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR or
                    CacheDataSource.FLAG_IGNORE_CACHE_FOR_UNSET_LENGTH_REQUESTS,
            )

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(cacheDataSourceFactory)
            .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
        playerBuilder.setMediaSourceFactory(mediaSourceFactory)

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

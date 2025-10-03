package net.primal.android.core.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import javax.inject.Inject
import net.primal.android.core.di.StreamVideoCache
import net.primal.android.core.service.PlayerManager
import net.primal.android.core.service.PrimalCacheKeyFactory
import net.primal.android.stream.player.LIVE_STREAM_MANIFEST_MIN_RETRY_COUNT
import net.primal.android.stream.player.SEEK_BACK_MS
import net.primal.android.stream.player.SEEK_FORWARD_MS

@OptIn(UnstableApi::class)
class AospPlayerManager @Inject constructor(
    private val loadControl: LoadControl,
    @param:StreamVideoCache private val simpleCache: SimpleCache,
) : PlayerManager {

    @OptIn(UnstableApi::class)
    override fun createPlayer(context: Context): Player {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        val loadErrorHandlingPolicy =
            DefaultLoadErrorHandlingPolicy(LIVE_STREAM_MANIFEST_MIN_RETRY_COUNT)

        val upstreamDataSourceFactory = DefaultDataSource.Factory(context)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setCacheKeyFactory(PrimalCacheKeyFactory)
            .setUpstreamDataSourceFactory(upstreamDataSourceFactory)
            .setFlags(
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR or
                    CacheDataSource.FLAG_IGNORE_CACHE_FOR_UNSET_LENGTH_REQUESTS,
            )

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
            .setDataSourceFactory(cacheDataSourceFactory)

        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .setSeekBackIncrementMs(SEEK_BACK_MS)
            .setSeekForwardIncrementMs(SEEK_FORWARD_MS)
            .build()
    }

    override fun cleanup() = Unit
}

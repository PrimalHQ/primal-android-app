package net.primal.android.core.video

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_VIDEO_WIDTH = 1280
private const val MAX_VIDEO_HEIGHT = 720

@Singleton
class PrimalVideoPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var _exoPlayer: ExoPlayer? = null

    @OptIn(UnstableApi::class)
    fun getOrCreateExoPlayer(): ExoPlayer {
        return _exoPlayer ?: run {
            val cache = VideoCache.getInstance(context)
            val cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

            val mediaSourceFactory = DefaultMediaSourceFactory(context)
                .setDataSourceFactory(cacheDataSourceFactory)

            val trackSelector = DefaultTrackSelector(context).apply {
                parameters = DefaultTrackSelector.Parameters.Builder()
                    .setMaxVideoSize(MAX_VIDEO_WIDTH, MAX_VIDEO_HEIGHT)
                    // More aggressive settings for .mov files and memory optimization
                    .setMaxVideoBitrate(2000000) // Limit bitrate to 2Mbps
                    .setForceLowestBitrate(false)
                    .setForceHighestSupportedBitrate(false)
                    .build()
            }

            ExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .setMediaSourceFactory(mediaSourceFactory)
                // Add load control for better memory management
                .setLoadControl(
                    androidx.media3.exoplayer.DefaultLoadControl.Builder()
                        .setBufferDurationsMs(
                            15000, // Min buffer (15s)
                            30000, // Max buffer (30s) - reduced from default
                            1500, // Buffer for playback
                            5000, // Buffer for rebuffer
                        )
                        .build(),
                )
                .build().also { player ->
                    _exoPlayer = player
                }
        }
    }

    fun releasePlayer() {
        _exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            player.release()
        }
        _exoPlayer = null
    }

    fun clearMediaItems() {
        _exoPlayer?.clearMediaItems()
    }

    fun stopPlayback() {
        _exoPlayer?.stop()
    }

    // Add method to force garbage collection after clearing media
    fun forceCleanup() {
        _exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            player.release()
        }
        // Suggest garbage collection for heavy codec cleanup
        System.gc()
    }
}

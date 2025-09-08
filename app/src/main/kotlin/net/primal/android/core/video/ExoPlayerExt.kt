package net.primal.android.core.video

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import net.primal.android.core.video.PlaybackConstants.BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MSEC
import net.primal.android.core.video.PlaybackConstants.BUFFER_FOR_PLAYBACK_MSEC
import net.primal.android.core.video.PlaybackConstants.MAX_BITRATE
import net.primal.android.core.video.PlaybackConstants.MAX_BUFFER_MSEC
import net.primal.android.core.video.PlaybackConstants.MAX_VIDEO_HEIGHT
import net.primal.android.core.video.PlaybackConstants.MAX_VIDEO_WIDTH
import net.primal.android.core.video.PlaybackConstants.MIN_BUFFER_MSEC
import net.primal.domain.nostr.Naddr

private object PlaybackConstants {
    const val MAX_VIDEO_WIDTH = 1280
    const val MAX_VIDEO_HEIGHT = 720
    const val MAX_BITRATE = 2_000_000

    const val MIN_BUFFER_MSEC = 10_000
    const val MAX_BUFFER_MSEC = 30_000
    const val BUFFER_FOR_PLAYBACK_MSEC = 2_500
    const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MSEC = 5_000
}

@UnstableApi
@Composable
fun rememberPrimalExoPlayer(): ExoPlayer {
    val context = LocalContext.current
    return remember { initializePlayer(context) }
}

@UnstableApi
fun initializePlayer(context: Context): ExoPlayer {
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
            .setMaxVideoBitrate(MAX_BITRATE)
            .setForceLowestBitrate(false)
            .setForceHighestSupportedBitrate(false)
            .build()
    }

    val renderersFactory = DefaultRenderersFactory(context).apply {
        setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
    }

    return ExoPlayer.Builder(context)
        .setMediaSourceFactory(mediaSourceFactory)
        .setTrackSelector(trackSelector)
        .setRenderersFactory(renderersFactory)
        .setLoadControl(
            DefaultLoadControl.Builder()
                .setPrioritizeTimeOverSizeThresholds(true)
                .setBufferDurationsMs(
                    MIN_BUFFER_MSEC,
                    MAX_BUFFER_MSEC,
                    BUFFER_FOR_PLAYBACK_MSEC,
                    BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MSEC,
                )
                .build(),
        )
        .build()
}

@Composable
fun rememberPrimalStreamExoPlayer(
    streamNaddr: Naddr,
    onIsPlayingChanged: (ExoPlayer, Boolean) -> Unit,
    onPlaybackStateChanged: (ExoPlayer, Int) -> Unit,
    onPlayerError: (errorCode: Int) -> Unit,
): ExoPlayer {
    val context = LocalContext.current
    val exoPlayer = remember(streamNaddr) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .build()
    }

    DisposableEffect(exoPlayer, onIsPlayingChanged, onPlaybackStateChanged, onPlayerError) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onIsPlayingChanged(exoPlayer, isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                onPlaybackStateChanged(exoPlayer, playbackState)
            }

            override fun onPlayerError(error: PlaybackException) {
                onPlayerError(error.errorCode)
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    return exoPlayer
}

@OptIn(UnstableApi::class)
@Composable
fun rememberMediaSession(exoPlayer: ExoPlayer): MediaSession {
    val context = LocalContext.current

    val mediaSession = remember(context, exoPlayer) {
        MediaSession.Builder(context, exoPlayer).build()
    }

    DisposableEffect(mediaSession) {
        onDispose { mediaSession.release() }
    }

    return mediaSession
}

fun ExoPlayer.toggle() =
    if (isPlaying) {
        pause()
    } else {
        play()
    }

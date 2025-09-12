package net.primal.android.core.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.ListenableFuture
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import net.primal.android.MainActivity
import net.primal.android.networking.UserAgentProvider
import net.primal.core.utils.AndroidBuildConfig
import org.chromium.net.CronetEngine
import timber.log.Timber

class PrimalMediaSessionService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var cronetEngine: CronetEngine? = null
    private var cronetExecutor: ExecutorService? = null

    @UnstableApi
    private class PrimalStreamAnalyticsListener : AnalyticsListener {
        private companion object {
            const val BITS_IN_KILOBIT = 1000
            const val BYTES_IN_KILOBYTE = 1024
        }

        private var isUsingCronet: Boolean = false
        private var cronetVersion: String = "N/A"

        fun setCronetInfo(version: String) {
            isUsingCronet = true
            cronetVersion = version
        }

        override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: PlaybackException) {
            Timber.e(error, "Player Error at ${eventTime.realtimeMs}:")
        }

        override fun onLoadError(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
            error: IOException,
            wasCanceled: Boolean,
        ) {
            if (!wasCanceled) {
                Timber.w(error, "Network Load Error at ${eventTime.realtimeMs}: uri=${loadEventInfo.uri}")
            }
        }

        override fun onBandwidthEstimate(
            eventTime: AnalyticsListener.EventTime,
            totalLoadTimeMs: Int,
            totalBytesLoaded: Long,
            bitrateEstimate: Long,
        ) {
            Timber.d(
                "Bandwidth Estimate: ${bitrateEstimate / BITS_IN_KILOBIT} kbps " +
                    "(loaded ${totalBytesLoaded / BYTES_IN_KILOBYTE} KB in ${totalLoadTimeMs}ms)",
            )
        }

        override fun onDroppedVideoFrames(
            eventTime: AnalyticsListener.EventTime,
            droppedFrames: Int,
            elapsedMs: Long,
        ) {
            if (droppedFrames > 0) {
                Timber.w("Dropped $droppedFrames video frames in ${elapsedMs}ms")
            }
        }

        override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
            val stateString = when (state) {
                Player.STATE_IDLE -> "IDLE"
                Player.STATE_BUFFERING -> "BUFFERING"
                Player.STATE_READY -> "READY"
                Player.STATE_ENDED -> "ENDED"
                else -> "?"
            }
            Timber.d("Playback state changed to: $stateString")
        }

        override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
            Timber.d("Is playing changed to: $isPlaying")
        }

        override fun onDownstreamFormatChanged(eventTime: AnalyticsListener.EventTime, mediaLoadData: MediaLoadData) {
            val format = mediaLoadData.trackFormat
            if (format != null) {
                Timber.d(
                    "Downstream format changed: Resolution=%dx%d, Bitrate=%d, MimeType=%s",
                    format.width,
                    format.height,
                    format.bitrate,
                    format.sampleMimeType,
                )
            }
        }

        override fun onRenderedFirstFrame(
            eventTime: AnalyticsListener.EventTime,
            output: Any,
            renderTimeMs: Long,
        ) {
            Timber.d("Rendered first frame in ${renderTimeMs}ms. Using Cronet: $isUsingCronet (v: $cronetVersion)")
        }
    }

    override fun onCreate() {
        super.onCreate()
        val player = createPlayer()
        mediaSession = buildMediaSession(player)
    }

    @OptIn(UnstableApi::class)
    private fun createPlayer(): Player {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        val analyticsListener = PrimalStreamAnalyticsListener()

        val playerBuilder = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)

        cronetEngine = runCatching {
            CronetEngine.Builder(this)
                .enableHttp2(true)
                .enableQuic(true)
                .enableBrotli(true)
                .setUserAgent("${UserAgentProvider.APP_NAME}/${AndroidBuildConfig}")
                .build()
        }.onSuccess { engine ->
            val executor = Executors.newSingleThreadExecutor()
            this.cronetExecutor = executor

            val dataSourceFactory = CronetDataSource.Factory(engine, executor)
            val mediaSourceFactory = DefaultMediaSourceFactory(this).setDataSourceFactory(dataSourceFactory)
            playerBuilder.setMediaSourceFactory(mediaSourceFactory)

            val version = try {
                engine.versionString
            } catch (_: Exception) {
                "unknown"
            }
            analyticsListener.setCronetInfo(version)

            Timber.i("Cronet enabled for media playback: $version")
        }.onFailure {
            Timber.w(it, "Cronet is not available. Using default HTTP stack for media playback.")
        }.getOrNull()

        val player = playerBuilder.build()
        player.addAnalyticsListener(analyticsListener)

        return player
    }

    private fun buildMediaSession(player: Player): MediaSession {
        return MediaSession.Builder(this, player)
            .setCallback(
                object : MediaSession.Callback {
                    @OptIn(UnstableApi::class)
                    override fun onSetMediaItems(
                        mediaSession: MediaSession,
                        controller: MediaSession.ControllerInfo,
                        mediaItems: List<MediaItem>,
                        startIndex: Int,
                        startPositionMs: Long,
                    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
                        mediaItems.firstOrNull()?.let {
                            mediaSession.setSessionActivity(
                                deepLinkPendingIntent(
                                    this@PrimalMediaSessionService,
                                    it.mediaId,
                                ),
                            )
                        }

                        return super.onSetMediaItems(mediaSession, controller, mediaItems, startIndex, startPositionMs)
                    }

                    @OptIn(UnstableApi::class)
                    override fun onConnect(
                        session: MediaSession,
                        controller: MediaSession.ControllerInfo,
                    ): MediaSession.ConnectionResult {
                        val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
                            .buildUpon()
                            .remove(COMMAND_SEEK_TO_PREVIOUS)
                            .remove(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                            .remove(COMMAND_SEEK_TO_NEXT)
                            .remove(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                            .build()

                        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                            .setAvailableSessionCommands(MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS)
                            .setAvailablePlayerCommands(playerCommands)
                            .build()
                    }
                },
            )
            .build()
    }

    private fun deepLinkPendingIntent(context: Context, mediaId: String): PendingIntent {
        val uri = "primal://live/$mediaId".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
            )
        }
        return PendingIntent.getActivity(
            context,
            mediaId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }

        cronetEngine?.shutdown()
        cronetExecutor?.shutdownNow()
        cronetEngine = null
        cronetExecutor = null

        super.onDestroy()
    }
}

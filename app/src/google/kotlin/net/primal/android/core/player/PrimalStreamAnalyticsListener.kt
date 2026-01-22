package net.primal.android.core.player

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import io.github.aakira.napier.Napier
import java.io.IOException

@UnstableApi
class PrimalStreamAnalyticsListener : AnalyticsListener {

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
        Napier.e(message = "Player Error at ${eventTime.realtimeMs}:", throwable = error)
    }

    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean,
    ) {
        if (!wasCanceled) {
            Napier.w(
                message = "Network Load Error at ${eventTime.realtimeMs}: uri=${loadEventInfo.uri}",
                throwable = error,
            )
        }
    }

    override fun onBandwidthEstimate(
        eventTime: AnalyticsListener.EventTime,
        totalLoadTimeMs: Int,
        totalBytesLoaded: Long,
        bitrateEstimate: Long,
    ) {
        Napier.d {
            "Bandwidth Estimate: ${bitrateEstimate / BITS_IN_KILOBIT} kbps " +
                "(loaded ${totalBytesLoaded / BYTES_IN_KILOBYTE} KB in ${totalLoadTimeMs}ms)"
        }
    }

    override fun onDroppedVideoFrames(
        eventTime: AnalyticsListener.EventTime,
        droppedFrames: Int,
        elapsedMs: Long,
    ) {
        if (droppedFrames > 0) {
            Napier.w { "Dropped $droppedFrames video frames in ${elapsedMs}ms" }
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
        Napier.d { "Playback state changed to: $stateString" }
    }

    override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
        Napier.d { "Is playing changed to: $isPlaying" }
    }

    override fun onDownstreamFormatChanged(eventTime: AnalyticsListener.EventTime, mediaLoadData: MediaLoadData) {
        val format = mediaLoadData.trackFormat
        if (format != null) {
            Napier.d {
                "Downstream format changed: Resolution=${format.width}x${format.height}, " +
                    "Bitrate=${format.bitrate}, MimeType=${format.sampleMimeType}"
            }
        }
    }

    override fun onRenderedFirstFrame(
        eventTime: AnalyticsListener.EventTime,
        output: Any,
        renderTimeMs: Long,
    ) {
        Napier.d { "Rendered first frame in ${renderTimeMs}ms. Using Cronet: $isUsingCronet (v: $cronetVersion)" }
    }
}

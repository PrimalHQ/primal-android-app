package net.primal.android.core.player

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import java.io.IOException
import timber.log.Timber

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

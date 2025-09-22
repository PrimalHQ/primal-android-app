package net.primal.android.stream.utils

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import net.primal.android.stream.LiveStreamContract
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV.toNaddrString

private object LivePlaybackConstants {
    const val TARGET_OFFSET_MS = 5_000L

    const val MIN_OFFSET_MS = 1_500L

    const val MAX_OFFSET_MS = 8_000L
}

fun buildMediaItem(
    naddr: Naddr?,
    streamUrl: String,
    streamInfo: LiveStreamContract.StreamInfoUi?,
): MediaItem {
    val liveConfiguration = MediaItem.LiveConfiguration.Builder()
        .setTargetOffsetMs(LivePlaybackConstants.TARGET_OFFSET_MS)
        .setMinOffsetMs(LivePlaybackConstants.MIN_OFFSET_MS)
        .setMaxOffsetMs(LivePlaybackConstants.MAX_OFFSET_MS)
        .build()

    return MediaItem.Builder()
        .run { if (naddr != null) this.setMediaId(naddr.toNaddrString()) else this }
        .setUri(streamUrl)
        .setLiveConfiguration(liveConfiguration)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(streamInfo?.title)
                .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
                .setArtist(streamInfo?.mainHostProfile?.authorDisplayName)
                .setArtworkUri(streamInfo?.image?.toUri())
                .build(),
        ).build()
}

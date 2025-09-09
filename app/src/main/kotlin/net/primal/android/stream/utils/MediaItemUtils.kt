package net.primal.android.stream.utils

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import net.primal.android.stream.LiveStreamContract

fun buildMediaItem(streamUrl: String, streamInfo: LiveStreamContract.StreamInfoUi?) =
    MediaItem.Builder()
        .setUri(streamUrl)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(streamInfo?.title)
                .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
                .setArtist(streamInfo?.mainHostProfile?.authorDisplayName)
                .setArtworkUri(streamInfo?.image?.toUri())
                .build(),
        ).build()

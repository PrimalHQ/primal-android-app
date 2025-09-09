package net.primal.android.stream.utils

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import net.primal.android.stream.LiveStreamContract
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV.toNaddrString

fun buildMediaItem(
    naddr: Naddr?,
    streamUrl: String,
    streamInfo: LiveStreamContract.StreamInfoUi?,
) = MediaItem.Builder()
    .run { if (naddr != null) this.setMediaId(naddr.toNaddrString()) else this }
    .setUri(streamUrl)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(streamInfo?.title)
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
            .setArtist(streamInfo?.mainHostProfile?.authorDisplayName)
            .setArtworkUri(streamInfo?.image?.toUri())
            .build(),
    ).build()

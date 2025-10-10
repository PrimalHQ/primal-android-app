package net.primal.android.stream.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.media3.common.Player
import net.primal.android.stream.LiveStreamContract
import net.primal.android.stream.utils.buildMediaItem
import net.primal.domain.nostr.Naddr

@Composable
fun StreamPlaybackController(
    mediaController: Player,
    playbackUrl: String?,
    naddr: Naddr?,
    streamInfo: LiveStreamContract.StreamInfoUi?,
) {
    LaunchedEffect(playbackUrl) {
        val currentMediaItemUri = mediaController.currentMediaItem?.localConfiguration?.uri?.toString()

        if (currentMediaItemUri != playbackUrl) {
            mediaController.stop()

            if (playbackUrl != null) {
                val mediaItem = buildMediaItem(naddr, playbackUrl, streamInfo)
                mediaController.setMediaItem(mediaItem)
                mediaController.repeatMode = Player.REPEAT_MODE_OFF
                mediaController.prepare()
                mediaController.playWhenReady = true
            } else {
                mediaController.clearMediaItems()
            }
        }
    }
}

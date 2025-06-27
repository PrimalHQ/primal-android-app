package net.primal.android.notes.feed.note.ui.attachment

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import coil3.compose.SubcomposeAsyncImage
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Mute
import net.primal.android.core.compose.icons.primaliconpack.Play
import net.primal.android.core.compose.icons.primaliconpack.Unmute
import net.primal.android.core.video.VideoCache
import net.primal.android.theme.AppTheme
import net.primal.android.user.domain.ContentDisplaySettings

@Composable
fun NoteAttachmentVideoPreview(
    eventUri: EventUriUi,
    onVideoClick: (positionMs: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val autoPlay = LocalContentDisplaySettings.current.autoPlayVideos ==
        ContentDisplaySettings.AUTO_PLAY_VIDEO_ALWAYS

    if (autoPlay) {
        AutoPlayVideo(eventUri, onVideoClick, modifier)
    } else {
        VideoThumbnailImagePreview(eventUri, { onVideoClick(0) }, modifier)
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun AutoPlayVideo(
    eventUri: EventUriUi,
    onVideoClick: (positionMs: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val cacheDataSourceFactory = remember(context) {
        CacheDataSource.Factory()
            .setCache(VideoCache.getInstance(context))
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
    }

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .build()
    }

    var isMuted by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(false) }

    LaunchedEffect(eventUri.url) {
        val mediaUrl = eventUri.variants?.firstOrNull()?.mediaUrl ?: eventUri.url
        exoPlayer.setMediaItem(MediaItem.fromUri(mediaUrl))
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer.playWhenReady = true
        exoPlayer.volume = if (isMuted) 0f else 1f
        exoPlayer.prepare()
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier.clip(AppTheme.shapes.medium),
        contentAlignment = Alignment.BottomEnd,
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onVideoClick(exoPlayer.currentPosition) },
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                }
            },
        )

        if (isBuffering) {
            PrimalLoadingSpinner(
                size = 48.dp,
            )
        }

        AudioButton(
            modifier = Modifier
                .padding(8.dp)
                .size(32.dp),
            imageVector = if (isMuted) PrimalIcons.Unmute else PrimalIcons.Mute,
        ) {
            isMuted = !isMuted
            exoPlayer.volume = if (isMuted) 0f else 1f
        }
    }
}

@Composable
private fun AudioButton(
    modifier: Modifier,
    imageVector: ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.42f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.padding(4.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint = Color.White,
        )
    }
}

@Composable
private fun VideoThumbnailImagePreview(
    eventUri: EventUriUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mediaUrl = eventUri.variants?.firstOrNull()?.mediaUrl ?: eventUri.url

    Box(
        modifier = modifier.clip(AppTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = eventUri.thumbnailUrl ?: mediaUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            loading = { PrimalLoadingSpinner(size = 24.dp) },
            error = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(AppTheme.extraColorScheme.surfaceVariantAlt3),
                )
            },
        )

        Icon(
            imageVector = PrimalIcons.Play,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(64.dp)
                .background(Color.Black.copy(alpha = 0.42f), CircleShape)
                .padding(start = 6.dp)
                .clickable { onClick() },
        )
    }
}

@Composable
fun PlayButton(loading: Boolean = false, onClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(color = Color.Black.copy(alpha = 0.42f), shape = CircleShape)
            .clip(CircleShape)
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() }),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            PrimalLoadingSpinner(
                size = 42.dp,
            )
        } else {
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .padding(start = 6.dp),
                imageVector = PrimalIcons.Play,
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
}

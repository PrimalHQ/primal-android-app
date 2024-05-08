package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.SubcomposeAsyncImage
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Mute
import net.primal.android.core.compose.icons.primaliconpack.Play
import net.primal.android.core.compose.icons.primaliconpack.Unmute
import net.primal.android.theme.AppTheme
import net.primal.android.user.domain.ContentDisplaySettings

@Composable
fun NoteAttachmentVideoPreview(
    attachment: NoteAttachmentUi,
    onVideoClick: (positionMs: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val autoPlay = LocalContentDisplaySettings.current.autoPlayVideos == ContentDisplaySettings.AUTO_PLAY_VIDEO_ALWAYS

    if (autoPlay) {
        AutoPlayVideo(
            modifier = modifier,
            attachment = attachment,
            onVideoClick = onVideoClick,
        )
    } else {
        VideoThumbnailImagePreview(
            modifier = modifier,
            attachment = attachment,
            onClick = { onVideoClick(0) },
        )
    }
}

@Composable
private fun AutoPlayVideo(
    attachment: NoteAttachmentUi,
    onVideoClick: (positionMs: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(true) }
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val mediaSource = remember(attachment) {
        val mediaUrl = attachment.variants?.firstOrNull()?.mediaUrl ?: attachment.url
        MediaItem.fromUri(mediaUrl)
    }

    LaunchedEffect(mediaSource) {
        exoPlayer.setMediaItem(mediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
        exoPlayer.volume = if (isMuted) 0.0f else 1.0f
    }

    DisposableEffect(mediaSource) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = modifier.clip(AppTheme.shapes.medium),
        contentAlignment = Alignment.BottomEnd,
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onVideoClick(exoPlayer.currentPosition) },
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                }
            },
        )

        AudioButton(
            modifier = Modifier
                .padding(all = 8.dp)
                .size(32.dp),
            imageVector = if (isMuted) PrimalIcons.Unmute else PrimalIcons.Mute,
            onClick = {
                if (isMuted) {
                    exoPlayer.volume = 1.0f
                    isMuted = false
                } else {
                    exoPlayer.volume = 0.0f
                    isMuted = true
                }
            },
        )
    }
}

@Composable
private fun AudioButton(
    modifier: Modifier,
    imageVector: ImageVector,
    onClick: () -> Unit,
    padding: Dp = 4.dp,
) {
    Box(
        modifier = modifier
            .background(color = Color.Black.copy(alpha = 0.42f), shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.padding(all = padding),
            imageVector = imageVector,
            contentDescription = null,
            tint = Color.White,
        )
    }
}

@Composable
private fun VideoThumbnailImagePreview(
    modifier: Modifier = Modifier,
    attachment: NoteAttachmentUi,
    onClick: () -> Unit,
) {
    val mediaUrl = attachment.variants?.firstOrNull()?.mediaUrl ?: attachment.url
    Box(
        modifier = modifier.clip(AppTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = attachment.thumbnailUrl ?: mediaUrl,
            modifier = modifier,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            loading = { NoteImageLoadingPlaceholder() },
            error = { NoteVideoThumbnailErrorImage() },
        )

        PlayButton(
            onClick = onClick,
        )
    }
}

@Composable
private fun PlayButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(color = Color.Black.copy(alpha = 0.42f), shape = CircleShape)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
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

@Composable
fun NoteVideoThumbnailErrorImage() {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppTheme.colorScheme.surface),
    )
}

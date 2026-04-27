package net.primal.android.notes.feed.note.ui.attachment

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import kotlinx.coroutines.delay
import net.primal.android.R
import net.primal.android.core.activity.LocalContentDisplaySettings
import net.primal.android.core.compose.PrimalAsyncImage
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Mute
import net.primal.android.core.compose.icons.primaliconpack.Play
import net.primal.android.core.compose.icons.primaliconpack.Unmute
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.video.rememberPrimalExoPlayer
import net.primal.android.stream.player.LocalStreamState
import net.primal.android.theme.AppTheme
import net.primal.android.user.domain.ContentDisplaySettings

private const val POSITION_POLL_INTERVAL_MS = 500L
private const val BADGE_AUTO_HIDE_DELAY_MS = 3_000L
private const val BADGE_FADE_DURATION_MS = 200
private const val MS_PER_SECOND = 1_000.0

@Composable
fun NoteAttachmentVideoPreview(
    eventUri: EventUriUi,
    onVideoClick: (positionMs: Long) -> Unit,
    allowAutoPlay: Boolean,
    couldAutoPlay: Boolean,
    modifier: Modifier = Modifier,
    onVideoSoundToggle: ((soundOn: Boolean) -> Unit)? = null,
) {
    val streamState = LocalStreamState.current
    val userPrefersAutoPlay =
        LocalContentDisplaySettings.current.autoPlayVideos == ContentDisplaySettings.AUTO_PLAY_VIDEO_ALWAYS

    val shouldAutoPlay = userPrefersAutoPlay && allowAutoPlay && !streamState.isActive()

    if (shouldAutoPlay) {
        AutoPlayVideo(
            eventUri = eventUri,
            playing = couldAutoPlay,
            onVideoClick = onVideoClick,
            modifier = modifier,
            onVideoSoundToggle = onVideoSoundToggle,
        )
    } else {
        VideoThumbnailImagePreview(
            eventUri = eventUri,
            onClick = { onVideoClick(0) },
            modifier = modifier,
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun AutoPlayVideo(
    eventUri: EventUriUi,
    playing: Boolean,
    onVideoClick: (positionMs: Long) -> Unit,
    modifier: Modifier = Modifier,
    onVideoSoundToggle: ((soundOn: Boolean) -> Unit)? = null,
) {
    val exoPlayer = rememberPrimalExoPlayer()
    val userPrefersSound = LocalContentDisplaySettings.current.autoPlayVideoSoundOn

    var isMuted by remember(userPrefersSound) { mutableStateOf(!userPrefersSound) }
    var isBuffering by remember { mutableStateOf(true) }
    var isPlayingState by remember { mutableStateOf(false) }

    DisposableLifecycleObserverEffect(key1 = exoPlayer) { event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> if (playing) exoPlayer.play()
            Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
            else -> Unit
        }
    }

    LaunchedEffect(eventUri.url) {
        val mediaUrl = eventUri.variants?.firstOrNull()?.mediaUrl ?: eventUri.url
        exoPlayer.apply {
            setMediaItem(MediaItem.fromUri(mediaUrl))
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlayingState = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(playing) {
        if (playing) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    val positionMs = rememberPlayerPositionMs(exoPlayer = exoPlayer, isPlaying = isPlayingState)
    val badgeVisible = rememberAutoHideBadgeVisibility(playerKey = exoPlayer, isPlaying = isPlayingState)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        PlayerSurface(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onVideoClick(exoPlayer.currentPosition) },
            player = exoPlayer,
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
        )

        if (isBuffering) {
            PrimalLoadingSpinner(size = 48.dp)
        }

        AutoPlayDurationBadgeOverlay(
            durationSeconds = eventUri.durationInSeconds,
            isPlaying = isPlayingState,
            positionMs = positionMs,
            visible = badgeVisible,
        )

        AudioButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(32.dp),
            imageVector = if (isMuted) PrimalIcons.Unmute else PrimalIcons.Mute,
        ) {
            val newMutedState = !isMuted
            isMuted = newMutedState
            onVideoSoundToggle?.invoke(!newMutedState)
        }
    }
}

@Composable
private fun rememberPlayerPositionMs(exoPlayer: Player, isPlaying: Boolean): Long {
    var positionMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(exoPlayer, isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                positionMs = exoPlayer.currentPosition
                delay(POSITION_POLL_INTERVAL_MS)
            }
        } else {
            positionMs = exoPlayer.currentPosition
        }
    }
    return positionMs
}

@Composable
private fun rememberAutoHideBadgeVisibility(playerKey: Any, isPlaying: Boolean): Boolean {
    var visible by remember(playerKey) { mutableStateOf(true) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            visible = true
            delay(BADGE_AUTO_HIDE_DELAY_MS)
            visible = false
        } else {
            visible = true
        }
    }
    return visible
}

@Composable
private fun BoxScope.AutoPlayDurationBadgeOverlay(
    durationSeconds: Double?,
    isPlaying: Boolean,
    positionMs: Long,
    visible: Boolean,
) {
    if (durationSeconds == null || durationSeconds <= 0.0) return
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(BADGE_FADE_DURATION_MS)),
        exit = fadeOut(animationSpec = tween(BADGE_FADE_DURATION_MS)),
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp),
    ) {
        VideoDurationBadge(
            durationSeconds = durationSeconds,
            playbackPositionMs = if (isPlaying) positionMs else null,
        )
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
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        PrimalAsyncImage(
            model = eventUri.thumbnailUrl,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            errorColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
        )

        PlayButton(onClick = onClick)

        val duration = eventUri.durationInSeconds
        if (duration != null && duration > 0.0) {
            VideoDurationBadge(
                durationSeconds = duration,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
fun PlayButton(loading: Boolean = false, onClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(color = Color.Black.copy(alpha = 0.42f), shape = CircleShape)
            .clip(CircleShape)
            .clickable(enabled = onClick != null && !loading, onClick = { onClick?.invoke() }),
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

@Composable
private fun VideoDurationBadge(
    durationSeconds: Double,
    modifier: Modifier = Modifier,
    playbackPositionMs: Long? = null,
) {
    val displaySeconds = if (playbackPositionMs == null) {
        durationSeconds
    } else {
        (durationSeconds - playbackPositionMs / MS_PER_SECOND).coerceAtLeast(0.0)
    }
    val formatted = formatVideoDuration(displaySeconds)
    val description = stringResource(R.string.accessibility_video_duration, formatted)

    Box(
        modifier = modifier
            .widthIn(min = 28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = formatted,
            style = AppTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
            color = Color.White,
        )
    }
}

package net.primal.android.core.service

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import net.primal.domain.nostr.Naddr

@Stable
internal class MediaControllerManager private constructor(context: Context) : RememberObserver {
    private val appContext = context.applicationContext
    private var factory: ListenableFuture<MediaController>? = null
    var controller by mutableStateOf<MediaController?>(null)
        private set

    init {
        initialize()
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    internal fun initialize() {
        if (factory == null || factory?.isDone == true) {
            factory = MediaController.Builder(
                appContext,
                SessionToken(appContext, ComponentName(appContext, PrimalMediaSessionService::class.java)),
            ).buildAsync()
        }
        factory?.addListener(
            {
                controller = factory?.let {
                    if (it.isDone) {
                        it.get()
                    } else {
                        null
                    }
                }
            },
            MoreExecutors.directExecutor(),
        )
    }

    internal fun release() {
        factory?.let {
            MediaController.releaseFuture(it)
            controller = null
        }
        factory = null
    }

    override fun onAbandoned() {
        release()
    }

    override fun onForgotten() {
        release()
    }

    override fun onRemembered() = Unit

    companion object {
        @Volatile
        private var instance: MediaControllerManager? = null

        fun getInstance(context: Context): MediaControllerManager {
            return instance ?: synchronized(this) {
                instance ?: MediaControllerManager(context).also { instance = it }
            }
        }
    }
}

@Composable
fun rememberManagedMediaController(
    streamNaddr: Naddr,
    onIsPlayingChanged: (Long?, Boolean) -> Unit,
    onPlaybackStateChanged: (Long?, Int) -> Unit,
    onPlayerError: (errorCode: Int) -> Unit,
): MediaController? {
    val appContext = LocalContext.current.applicationContext
    val controllerManager = remember(streamNaddr) {
        MediaControllerManager.getInstance(appContext).also { it.initialize() }
    }

    DisposableEffect(
        streamNaddr,
        controllerManager.controller,
        onIsPlayingChanged,
        onPlaybackStateChanged,
        onPlayerError,
    ) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onIsPlayingChanged(controllerManager.controller?.currentPosition, isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                onPlaybackStateChanged(controllerManager.controller?.duration, playbackState)
            }

            override fun onPlayerError(error: PlaybackException) {
                onPlayerError(error.errorCode)
            }
        }

        controllerManager.controller?.addListener(listener)
        onDispose {
            controllerManager.controller?.removeListener(listener)
            controllerManager.controller?.clearMediaItems()
        }
    }

    return controllerManager.controller
}

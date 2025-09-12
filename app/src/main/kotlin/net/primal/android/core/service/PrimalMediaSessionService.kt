package net.primal.android.core.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.EntryPointAccessors
import net.primal.android.MainActivity
import net.primal.android.core.service.di.MediaSessionServiceEntryPoint

class PrimalMediaSessionService : MediaSessionService() {

    private lateinit var playerManager: PlayerManager

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            MediaSessionServiceEntryPoint::class.java,
        )
        playerManager = entryPoint.playerManager()

        super.onCreate()
        val player = playerManager.createPlayer(this)
        mediaSession = buildMediaSession(player)
    }

    private fun buildMediaSession(player: Player): MediaSession {
        return MediaSession.Builder(this, player)
            .setCallback(
                object : MediaSession.Callback {
                    @OptIn(UnstableApi::class)
                    override fun onSetMediaItems(
                        mediaSession: MediaSession,
                        controller: MediaSession.ControllerInfo,
                        mediaItems: List<MediaItem>,
                        startIndex: Int,
                        startPositionMs: Long,
                    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
                        mediaItems.firstOrNull()?.let {
                            mediaSession.setSessionActivity(
                                deepLinkPendingIntent(
                                    this@PrimalMediaSessionService,
                                    it.mediaId,
                                ),
                            )
                        }

                        return super.onSetMediaItems(mediaSession, controller, mediaItems, startIndex, startPositionMs)
                    }

                    @OptIn(UnstableApi::class)
                    override fun onConnect(
                        session: MediaSession,
                        controller: MediaSession.ControllerInfo,
                    ): MediaSession.ConnectionResult {
                        val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
                            .buildUpon()
                            .remove(COMMAND_SEEK_TO_PREVIOUS)
                            .remove(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                            .remove(COMMAND_SEEK_TO_NEXT)
                            .remove(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                            .build()

                        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                            .setAvailableSessionCommands(MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS)
                            .setAvailablePlayerCommands(playerCommands)
                            .build()
                    }
                },
            )
            .build()
    }

    private fun deepLinkPendingIntent(context: Context, mediaId: String): PendingIntent {
        val uri = "primal://live/$mediaId".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
            )
        }
        return PendingIntent.getActivity(
            context,
            mediaId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        playerManager.cleanup()
        super.onDestroy()
    }
}

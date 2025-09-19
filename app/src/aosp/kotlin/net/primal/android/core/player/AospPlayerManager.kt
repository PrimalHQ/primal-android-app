package net.primal.android.core.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import javax.inject.Inject
import net.primal.android.core.service.PlayerManager

class AospPlayerManager
@OptIn(UnstableApi::class)
@Inject
constructor(
    private val loadControl: LoadControl,
) : PlayerManager {

    @OptIn(UnstableApi::class)
    override fun createPlayer(context: Context): Player {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setLoadControl(loadControl)
            .build()
    }

    override fun cleanup() {
    }
}

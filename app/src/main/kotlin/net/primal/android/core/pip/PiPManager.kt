package net.primal.android.core.pip

import android.app.PictureInPictureParams
import android.graphics.Rect
import android.os.Build
import android.util.Rational
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.primal.android.stream.player.VIDEO_ASPECT_RATIO_HEIGHT
import net.primal.android.stream.player.VIDEO_ASPECT_RATIO_WIDTH

@Stable
class PiPManager {
    var shouldEnterPiPMode by mutableStateOf(false)

    var sourceRectHint by mutableStateOf<Rect?>(null)

    fun buildPipParams(): PictureInPictureParams {
        val builder = PictureInPictureParams.Builder()

        builder.setSourceRectHint(sourceRectHint)
            .setAspectRatio(Rational(VIDEO_ASPECT_RATIO_WIDTH.toInt(), VIDEO_ASPECT_RATIO_HEIGHT.toInt()))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(shouldEnterPiPMode)
                .setSeamlessResizeEnabled(true)
        }

        return builder.build()
    }

    fun reset() {
        shouldEnterPiPMode = false
        sourceRectHint = null
    }
}

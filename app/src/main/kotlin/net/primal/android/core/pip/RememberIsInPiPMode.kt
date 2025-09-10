package net.primal.android.core.pip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer
import net.primal.android.core.utils.findComponentActivity

@Composable
fun rememberIsInPipMode(): Boolean {
    val activity = LocalContext.current.findComponentActivity()
    var pipMode by remember { mutableStateOf(activity?.isInPictureInPictureMode) }

    DisposableEffect(activity) {
        val observer = Consumer<PictureInPictureModeChangedInfo> { info ->
            pipMode = info.isInPictureInPictureMode
        }
        activity?.addOnPictureInPictureModeChangedListener(observer)
        onDispose { activity?.removeOnPictureInPictureModeChangedListener(observer) }
    }
    return pipMode == true
}

package net.primal.android.core.pip

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import net.primal.android.core.utils.findComponentActivity

@Composable
fun PiPManagerProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val componentActivity = context.findComponentActivity()
    val pipManager = remember { PiPManager() }

    val currentShouldEnterPipMode by rememberUpdatedState(newValue = pipManager.shouldEnterPiPMode)

    DisposableEffect(pipManager.shouldEnterPiPMode, pipManager.sourceRectHint, context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            val onUserLeaveBehavior = Runnable {
                if (currentShouldEnterPipMode) {
                    componentActivity?.enterPictureInPictureMode(pipManager.buildPipParams())
                }
            }
            componentActivity?.addOnUserLeaveHintListener(onUserLeaveBehavior)

            onDispose {
                componentActivity?.removeOnUserLeaveHintListener(onUserLeaveBehavior)
            }
        } else {
            componentActivity?.setPictureInPictureParams(pipManager.buildPipParams())

            onDispose { Unit }
        }
    }

    CompositionLocalProvider(LocalPiPManager provides pipManager) {
        content()
    }
}

val LocalPiPManager = staticCompositionLocalOf<PiPManager> {
    error("No PiPManager provided — did you forget to wrap your UI in PiPManagerProvider?")
}

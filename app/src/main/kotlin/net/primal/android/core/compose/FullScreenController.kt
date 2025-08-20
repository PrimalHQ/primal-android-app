package net.primal.android.core.compose

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.OrientationEventListener
import androidx.activity.compose.BackHandler
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import net.primal.android.core.compose.immersive.rememberImmersiveModeState

class FullScreenController(
    private val activity: Activity,
    private val onFullscreenChanged: (Boolean) -> Unit = {},
) {

    var isFullscreen = false
        private set

    private var hasUnlockedAfterPortrait = false
    private var hasUnlockedAfterLandscape = false

    @Suppress("MagicNumber")
    private fun isPortraitDeg(deg: Int): Boolean {
        return (deg in 0..25) || (deg in 335..359) || (deg in 155..205)
    }

    private val orientationListener = object : OrientationEventListener(activity) {
        override fun onOrientationChanged(orientation: Int) {
            if (orientation == ORIENTATION_UNKNOWN) return

            if (isFullscreen && !hasUnlockedAfterPortrait && isPortraitDeg(orientation)) {
                unlockRotation()
            }

            if (!isFullscreen && !hasUnlockedAfterLandscape && !isPortraitDeg(orientation)) {
                unlockRotation()
            }

            if (isFullscreen != !isPortraitDeg(orientation)) {
                isFullscreen = !isPortraitDeg(orientation)
                onFullscreenChanged(isFullscreen)
            }
        }
    }

    @MainThread
    fun enter() {
        if (isFullscreen) return
        isFullscreen = true
        hasUnlockedAfterPortrait = false
        hasUnlockedAfterLandscape = false
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onFullscreenChanged(true)
    }

    @MainThread
    fun toggle() {
        if (isFullscreen) exit() else enter()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @MainThread
    fun exit() {
        if (!isFullscreen) return
        isFullscreen = false
        hasUnlockedAfterPortrait = false
        hasUnlockedAfterLandscape = false
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        onFullscreenChanged(false)
    }

    private fun unlockRotation() {
        hasUnlockedAfterPortrait = true
        hasUnlockedAfterLandscape = true

        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }

    @Suppress("FunctionName")
    internal fun _init() {
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()
        }
    }

    @Suppress("FunctionName")
    internal fun _dispose() {
        orientationListener.disable()
    }
}

@Composable
fun rememberFullScreenController(onFullscreenChanged: (Boolean) -> Unit = {}): FullScreenController {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val activity = remember(context) { context as Activity }
    val immersiveMode = activity.window?.let { rememberImmersiveModeState(it) }
    val controller = remember(activity) { FullScreenController(activity, onFullscreenChanged) }

    BackHandler(enabled = controller.isFullscreen) {
        controller.exit()
    }

    DisposableEffect(Unit) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        controller._init()
        onDispose { controller._dispose() }
    }

    LaunchedEffect(configuration) {
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            immersiveMode?.show()
        } else {
            immersiveMode?.hide()
        }
    }

    return controller
}

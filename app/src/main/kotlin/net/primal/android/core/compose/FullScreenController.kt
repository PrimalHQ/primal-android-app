package net.primal.android.core.compose

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.provider.Settings
import android.view.OrientationEventListener
import androidx.activity.compose.BackHandler
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.core.compose.immersive.rememberImmersiveModeState

class FullScreenController(
    private val scope: CoroutineScope,
    private val activity: Activity,
    private val context: Context,
    private val onFullscreenChanged: (Boolean) -> Unit = {},
) {

    var isFullscreen = false
        private set

    var systemOrientation: Int = 0
        set(value) {
            isFullscreen = value == Configuration.ORIENTATION_LANDSCAPE
            field = value
        }

    private var hasUnlockedAfterPortrait = false
    private var hasUnlockedAfterLandscape = false

    @Suppress("MagicNumber")
    private fun isPortraitDeg(deg: Int): Boolean {
        return when (systemOrientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                (deg in 340..359) || (deg in 0..23)
            }

            Configuration.ORIENTATION_PORTRAIT -> {
                (deg in 292..359) || (deg in 0..68) || (deg in 114..247)
            }

            else -> false
        }
    }

    private val orientationListener = object : OrientationEventListener(activity) {
        override fun onOrientationChanged(orientation: Int) {
            if (orientation == ORIENTATION_UNKNOWN) return

            if (context.isAutoRotateEnabled()) {
                if (!isFullscreen && !hasUnlockedAfterPortrait && isPortraitDeg(orientation)) {
                    scope.launch {
                        delay(1.seconds)
                        unlockRotation()
                    }
                }

                if (isFullscreen && !hasUnlockedAfterLandscape && !isPortraitDeg(orientation)) {
                    scope.launch {
                        delay(1.seconds)
                        unlockRotation()
                    }
                }
            }
        }
    }

    @MainThread
    fun enter() {
        if (isFullscreen) return
        isFullscreen = true
        hasUnlockedAfterPortrait = true
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
        hasUnlockedAfterLandscape = true
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        onFullscreenChanged(false)
    }

    private fun unlockRotation() {
        hasUnlockedAfterPortrait = true
        hasUnlockedAfterLandscape = true

        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val activity = remember(context) { context as Activity }
    val immersiveMode = activity.window?.let { rememberImmersiveModeState(it) }
    val controller = remember(activity) { FullScreenController(scope, activity, context, onFullscreenChanged) }
    var initialRequestedOrientation: Int?

    BackHandler(enabled = controller.isFullscreen) {
        controller.exit()
    }

    DisposableEffect(Unit) {
        initialRequestedOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        controller._init()
        onDispose {
            controller._dispose()
            activity.requestedOrientation = initialRequestedOrientation
        }
    }

    LaunchedEffect(configuration) {
        controller.systemOrientation = configuration.orientation
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            immersiveMode?.show()
        } else {
            immersiveMode?.hide()
        }
    }

    return controller
}

private fun Context.isAutoRotateEnabled(): Boolean {
    return try {
        Settings.System.getInt(
            contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
        ) == 1
    } catch (_: Settings.SettingNotFoundException) {
        false
    }
}

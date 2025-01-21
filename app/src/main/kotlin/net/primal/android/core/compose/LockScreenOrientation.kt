package net.primal.android.core.compose

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
fun LockToOrientationPortrait() = LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

@Composable
fun LockToOrientationLandscape() = LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

@Composable
fun LockScreenOrientation(orientation: Int) {
    val activity = LocalActivity.current
    if (activity != null) {
        DisposableEffect(Unit) {
            val originalOrientation = activity.requestedOrientation
            activity.requestedOrientation = orientation
            onDispose {
                activity.requestedOrientation = originalOrientation
            }
        }
    }
}

package net.primal.android.core.compose

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun LockToOrientationPortrait() = LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

@Composable
fun LockToOrientationLandscape() = LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

@Composable
fun UnlockScreenOrientation() = LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)

@Composable
fun LockScreenOrientation(orientation: Int) {
    val activity = LocalActivity.current
    if (activity != null) {
        LaunchedEffect(orientation) {
            activity.requestedOrientation = orientation
        }
    }
}

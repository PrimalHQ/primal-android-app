package net.primal.android.core.utils

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

fun Context.hasNotificationPermission(channelId: String? = null): Boolean {
    if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) return false

    val isChannelEnabled = channelId?.let {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = notificationManager.getNotificationChannel(it)
        channel == null || channel.importance != NotificationManager.IMPORTANCE_NONE
    } ?: true

    val isPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    return isChannelEnabled && isPermissionGranted
}

fun Context.getNotificationSettingsIntent(channelId: String? = null): Intent {
    return Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        if (channelId != null) {
            putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
        }
    }
}

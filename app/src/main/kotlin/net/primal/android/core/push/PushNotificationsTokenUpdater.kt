package net.primal.android.core.push

interface PushNotificationsTokenUpdater {
    suspend fun updateTokenForAllUsers()
}

package net.primal.android.core.push

import javax.inject.Inject

class AospPushNotificationsTokenUpdater @Inject constructor() : PushNotificationsTokenUpdater {
    override suspend fun updateTokenForAllUsers() = Unit
    override suspend fun updateTokenForRemoteSigner() = Unit
    override suspend fun updateTokenForNwcService() = Unit
}

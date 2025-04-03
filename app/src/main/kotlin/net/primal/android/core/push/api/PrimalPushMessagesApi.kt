package net.primal.android.core.push.api

interface PrimalPushMessagesApi {

    suspend fun updateNotificationsToken(userIds: List<String>, token: String)
}

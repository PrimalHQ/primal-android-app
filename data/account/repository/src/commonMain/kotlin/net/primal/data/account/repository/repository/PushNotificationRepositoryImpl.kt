package net.primal.data.account.repository.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.remote.pushnotifications.PushNotificationApi
import net.primal.data.account.remote.pushnotifications.model.NotificationScope
import net.primal.domain.account.pushnotifications.PushNotificationRepository
import net.primal.domain.nostr.NostrEvent

class PushNotificationRepositoryImpl(
    private val dispatchers: DispatcherProvider,
    private val pushNotificationApi: PushNotificationApi,
) : PushNotificationRepository {

    override suspend fun updateNotificationsToken(authorizationEvents: List<NostrEvent>, token: String) {
        withContext(dispatchers.io()) {
            pushNotificationApi.updateNotificationsToken(authorizationEvents, token)
        }
    }

    override suspend fun updateNotificationTokenForNip46(authorizationEvent: NostrEvent, token: String) {
        withContext(dispatchers.io()) {
            pushNotificationApi.updateNotificationTokenForNip46OrNip47(listOf(authorizationEvent), token)
        }
    }

    override suspend fun updateNotificationTokenForNip47(authorizationEvents: List<NostrEvent>, token: String) {
        withContext(dispatchers.io()) {
            pushNotificationApi.updateNotificationTokenForNip46OrNip47(
                authorizationEvents = authorizationEvents,
                token = token,
                scope = NotificationScope.Nip47,
            )
        }
    }
}

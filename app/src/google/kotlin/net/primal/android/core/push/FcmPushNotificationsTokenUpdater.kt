package net.primal.android.core.push

import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.push.api.PrimalPushMessagesApi
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.core.networking.sockets.errors.WssException
import timber.log.Timber

class FcmPushNotificationsTokenUpdater @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val userAccountsStore: UserAccountsStore,
    private val primalPushMessagesApi: PrimalPushMessagesApi,
) : PushNotificationsTokenUpdater {
    override suspend fun updateTokenForAllUsers() {
        withContext(dispatcherProvider.io()) {
            runCatching {
                FirebaseMessaging.getInstance().token.await()
            }.getOrNull()?.let { token ->
                val userIds = userAccountsStore.userAccounts.value.map { it.pubkey }
                try {
                    primalPushMessagesApi.updateNotificationsToken(userIds = userIds, token = token)
                } catch (error: WssException) {
                    Timber.e(error)
                }
            }
        }
    }
}

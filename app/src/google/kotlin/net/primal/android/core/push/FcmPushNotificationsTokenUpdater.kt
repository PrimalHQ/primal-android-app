package net.primal.android.core.push

import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import net.primal.android.core.push.api.PrimalPushMessagesApi
import net.primal.android.core.push.api.model.UpdateTokenContent
import net.primal.android.networking.UserAgentProvider
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asIdentifierTag
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.getOrNull
import timber.log.Timber

class FcmPushNotificationsTokenUpdater @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val userAccountsStore: UserAccountsStore,
    private val primalPushMessagesApi: PrimalPushMessagesApi,
    private val signatureHandler: NostrEventSignatureHandler,
) : PushNotificationsTokenUpdater {
    override suspend fun updateTokenForAllUsers() {
        withContext(dispatcherProvider.io()) {
            runCatching {
                FirebaseMessaging.getInstance().token.await()
            }.getOrNull()?.let { token ->
                val userIds = userAccountsStore.userAccounts.value
                    .filter { it.pushNotificationsEnabled }.map { it.pubkey }

                val authorizationEvents = userIds.mapNotNull { userId ->
                    signAuthorizationEventOrNull(userId = userId, token = token)
                }

                try {
                    primalPushMessagesApi.updateNotificationsToken(
                        authorizationEvents = authorizationEvents,
                        token = token,
                    )
                } catch (error: NetworkException) {
                    Timber.e(error)
                }
            }
        }
    }

    private suspend fun signAuthorizationEventOrNull(userId: String, token: String): NostrEvent? {
        val signResult = signatureHandler.signNostrEvent(
            NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.ApplicationSpecificData.value,
                tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
                content = UpdateTokenContent(token = token).encodeToJsonString(),
            ),
        )
        return signResult.getOrNull()
    }
}

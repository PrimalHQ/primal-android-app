package net.primal.android.core.push

import com.google.firebase.messaging.FirebaseMessaging
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import net.primal.android.core.push.api.model.UpdateTokenContent
import net.primal.android.core.push.api.model.UpdateTokenContentNip46
import net.primal.android.networking.UserAgentProvider
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.asKeyPair
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.account.pushnotifications.PushNotificationRepository
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asIdentifierTag
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.getOrNull

class FcmPushNotificationsTokenUpdater @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val userAccountsStore: UserAccountsStore,
    private val credentialsStore: CredentialsStore,
    private val connectionRepository: ConnectionRepository,
    private val pushNotificationRepository: PushNotificationRepository,
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
                    signAuthorizationEventOrNull(
                        userId = userId,
                        content = UpdateTokenContent(token = token).encodeToJsonString(),
                    )
                }

                try {
                    pushNotificationRepository.updateNotificationsToken(
                        authorizationEvents = authorizationEvents,
                        token = token,
                    )
                } catch (error: NetworkException) {
                    Napier.e(throwable = error) { "Failed to update notifications token." }
                }
            }
        }
    }

    override suspend fun updateTokenForRemoteSigner() {
        withContext(dispatcherProvider.io()) {
            runCatching {
                FirebaseMessaging.getInstance().token.await()
            }.mapCatching { token ->
                val signerKeyPair = credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair()
                val connections = connectionRepository.getAllAutoStartConnections(signerPubKey = signerKeyPair.pubKey)

                signAuthorizationEventOrNull(
                    userId = signerKeyPair.pubKey,
                    content = UpdateTokenContentNip46(
                        token = token,
                        relays = connections.flatMap { it.relays }.toSet(),
                        clientPubKeys = connections.map { it.clientPubKey }.toSet(),
                    ).encodeToJsonString(),
                )?.let { authorizationEvent ->
                    pushNotificationRepository.updateNotificationTokenForNip46(
                        authorizationEvent = authorizationEvent,
                        token = token,
                    )
                }
            }.onFailure {
                Napier.e(throwable = it) { "Failed to update notification token for remote signer." }
            }
        }
    }

    private suspend fun signAuthorizationEventOrNull(userId: String, content: String): NostrEvent? {
        val signResult = signatureHandler.signNostrEvent(
            NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.ApplicationSpecificData.value,
                tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
                content = content,
            ),
        )
        return signResult.getOrNull()
    }
}

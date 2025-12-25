package net.primal.android.nostrconnect.handler

import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.asKeyPair
import net.primal.domain.account.repository.SessionEventRepository

private const val NIP46_EVENT_PUBKEY = "nip46_event_pubkey"
private const val NIP46_EVENT_ID = "nip46_event_id"

class RemoteSignerRemoteMessageHandler @Inject constructor(
    private val sessionHandler: RemoteSignerSessionHandler,
    private val sessionEventRepository: SessionEventRepository,
    private val credentialsStore: CredentialsStore,
) {
    fun isRemoteSignerMessage(message: RemoteMessage) =
        message.data[NIP46_EVENT_PUBKEY] != null && message.data[NIP46_EVENT_ID] != null

    suspend fun process(message: RemoteMessage) {
        val clientPubKey = message.data[NIP46_EVENT_PUBKEY]
        val eventId = message.data[NIP46_EVENT_ID]

        if (clientPubKey != null && eventId != null) {
            val autoStartEnabled = sessionHandler.isAutoStartEnabled(clientPubKey)
            if (autoStartEnabled) {
                val signerKeyPair = credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair()
                if (sessionHandler.hasActiveSession(clientPubKey)) {
                    sessionEventRepository.notifyMissedNostrEvents(signerKeyPair, listOf(eventId))
                } else {
                    sessionHandler.startSession(clientPubKey)
                        .onSuccess {
                            sessionEventRepository.notifyMissedNostrEvents(signerKeyPair, listOf(eventId))
                        }
                }
            }
        }
    }
}

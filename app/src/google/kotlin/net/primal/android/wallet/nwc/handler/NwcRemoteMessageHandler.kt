package net.primal.android.wallet.nwc.handler

import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import net.primal.domain.connections.nostr.NwcRepository
import net.primal.domain.nostr.NostrEventKind

private const val EVENT_PUBKEY = "event_pubkey"
private const val EVENT_ID = "event_id"
private const val EVENT_KIND = "event_kind"

class NwcRemoteMessageHandler @Inject constructor(
    private val sessionHandler: NwcSessionHandler,
    private val nwcRepository: NwcRepository,
) {
    fun isNwcMessage(message: RemoteMessage): Boolean =
        message.data[EVENT_KIND]?.toIntOrNull() == NostrEventKind.NwcRequest.value

    suspend fun process(message: RemoteMessage) {
        val secretPubKey = message.data[EVENT_PUBKEY] ?: return
        val requestId = message.data[EVENT_ID] ?: return
        val connection = nwcRepository.getConnection(secretPubKey = secretPubKey).getOrNull() ?: return

        val autoStartEnabled = nwcRepository.isAutoStartEnabledForUser(connection.userId)
        if (!autoStartEnabled) return

        if (!sessionHandler.isServiceRunningForUser(connection.userId)) {
            sessionHandler.startService(userId = connection.userId)
        }

        nwcRepository.notifyMissedNwcEvents(userId = connection.userId, eventIds = listOf(requestId))
    }
}

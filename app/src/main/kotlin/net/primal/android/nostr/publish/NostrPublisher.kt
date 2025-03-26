package net.primal.android.nostr.publish

import javax.inject.Inject
import net.primal.android.networking.relays.RelaysSocketManager
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.notary.exceptions.MissingPrivateKey
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.user.domain.Relay
import net.primal.android.wallet.nwc.model.LightningPayResponse
import net.primal.domain.nostr.ContentMetadata
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.publisher.NostrEventImporter
import net.primal.domain.publisher.PrimalPublishResult
import net.primal.domain.publisher.PrimalPublisher
import timber.log.Timber

class NostrPublisher @Inject constructor(
    private val relaysSocketManager: RelaysSocketManager,
    private val nostrNotary: NostrNotary,
    private val primalEventsImporter: NostrEventImporter,
) : PrimalPublisher {

    private suspend fun importEvent(event: NostrEvent): Boolean {
        val result = runCatching {
            primalEventsImporter.importEvents(events = listOf(event))
        }
        return result.isSuccess
    }

    @Throws(NostrPublishException::class, MissingPrivateKey::class)
    private suspend fun publishAndImportEvent(
        signedNostrEvent: NostrEvent,
        outboxRelays: List<String> = emptyList(),
    ): Boolean {
        relaysSocketManager.publishEvent(signedNostrEvent)
        if (outboxRelays.isNotEmpty()) {
            try {
                relaysSocketManager.publishEvent(
                    nostrEvent = signedNostrEvent,
                    relays = outboxRelays.map { Relay(url = it, read = false, write = true) },
                )
            } catch (error: NostrPublishException) {
                Timber.w(error, "Failed to publish to outbox relays.")
            }
        }
        return importEvent(signedNostrEvent)
    }

    @Throws(NostrPublishException::class, MissingPrivateKey::class)
    @Deprecated("Please use signPublishImportNostrEvent(NoNostrUnsignedEvent, List<String>).")
    suspend fun signPublishImportNostrEvent(
        userId: String,
        unsignedNostrEvent: NostrUnsignedEvent,
        outboxRelays: List<String> = emptyList(),
    ): PrimalPublishResult {
        val signedNostrEvent = nostrNotary.signNostrEvent(userId = userId, event = unsignedNostrEvent)
        val imported = publishAndImportEvent(signedNostrEvent = signedNostrEvent, outboxRelays = outboxRelays)
        return PrimalPublishResult(
            nostrEvent = signedNostrEvent,
            imported = imported,
        )
    }

    @Throws(NostrPublishException::class, MissingPrivateKey::class)
    override suspend fun signPublishImportNostrEvent(
        unsignedNostrEvent: NostrUnsignedEvent,
        outboxRelays: List<String>,
    ): PrimalPublishResult {
        return signPublishImportNostrEvent(
            userId = unsignedNostrEvent.pubKey,
            unsignedNostrEvent = unsignedNostrEvent,
            outboxRelays = outboxRelays,
        )
    }

    @Throws(NostrPublishException::class, MissingPrivateKeyException::class)
    suspend fun publishUserProfile(userId: String, contentMetadata: ContentMetadata): NostrEvent {
        val signedNostrEvent = nostrNotary.signMetadataNostrEvent(userId = userId, metadata = contentMetadata)
        relaysSocketManager.publishEvent(nostrEvent = signedNostrEvent)
        importEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class, MissingPrivateKey::class)
    suspend fun publishUserFollowList(
        userId: String,
        contacts: Set<String>,
        content: String,
    ): NostrEvent {
        val signedNostrEvent = nostrNotary.signFollowListNostrEvent(
            userId = userId,
            contacts = contacts,
            content = content,
        )
        publishAndImportEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class, MissingPrivateKey::class)
    suspend fun setMuteList(userId: String, muteList: Set<String>): NostrEvent {
        val signedNostrEvent = nostrNotary.signMuteListNostrEvent(userId = userId, mutedUserIds = muteList)
        publishAndImportEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class, MissingPrivateKey::class)
    suspend fun publishDirectMessage(
        userId: String,
        receiverId: String,
        encryptedContent: String,
    ): NostrEvent {
        val signedNostrEvent = nostrNotary.signEncryptedDirectMessage(
            userId = userId,
            receiverId = receiverId,
            encryptedContent = encryptedContent,
        )
        publishAndImportEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class, MissingPrivateKey::class)
    suspend fun publishRelayList(userId: String, relays: List<Relay>): NostrEvent {
        val signedNostrEvent = nostrNotary.signRelayListMetadata(userId = userId, relays = relays)
        relaysSocketManager.publishEvent(nostrEvent = signedNostrEvent, relays = relays)
        importEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class, MissingPrivateKey::class)
    suspend fun publishWalletRequest(invoice: LightningPayResponse, nwcData: NostrWalletConnect) {
        val walletPayNostrEvent = nostrNotary.signWalletInvoiceRequestNostrEvent(
            request = invoice.toWalletPayRequest(),
            nwc = nwcData,
        )
        relaysSocketManager.publishNwcEvent(nostrEvent = walletPayNostrEvent)
    }
}

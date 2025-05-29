package net.primal.android.nostr.publish

import javax.inject.Inject
import net.primal.android.networking.relays.RelaysSocketManager
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.domain.Relay
import net.primal.core.networking.nwc.model.NostrWalletConnect
import net.primal.core.networking.nwc.model.NwcWalletRequest
import net.primal.domain.nostr.ContentMetadata
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
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

    @Throws(NostrPublishException::class)
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

    @Throws(NostrPublishException::class, SignatureException::class)
    override suspend fun signPublishImportNostrEvent(
        unsignedNostrEvent: NostrUnsignedEvent,
        outboxRelays: List<String>,
    ): PrimalPublishResult {
        val signedNostrEvent = nostrNotary.signNostrEvent(unsignedNostrEvent = unsignedNostrEvent).unwrapOrThrow()
        val imported = publishAndImportEvent(signedNostrEvent = signedNostrEvent, outboxRelays = outboxRelays)
        return PrimalPublishResult(
            nostrEvent = signedNostrEvent,
            imported = imported,
        )
    }

    @Throws(NostrPublishException::class, SignatureException::class)
    suspend fun publishUserProfile(userId: String, contentMetadata: ContentMetadata): NostrEvent {
        val signedNostrEvent = nostrNotary
            .signMetadataNostrEvent(userId = userId, metadata = contentMetadata)
            .unwrapOrThrow()

        relaysSocketManager.publishEvent(nostrEvent = signedNostrEvent)
        importEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class, SignatureException::class)
    suspend fun publishUserFollowList(
        userId: String,
        contacts: Set<String>,
        content: String,
    ): NostrEvent {
        val signedNostrEvent = nostrNotary.signFollowListNostrEvent(
            userId = userId,
            contacts = contacts,
            content = content,
        ).unwrapOrThrow()

        publishAndImportEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class, SignatureException::class)
    suspend fun publishRelayList(userId: String, relays: List<Relay>): NostrEvent {
        val signedNostrEvent = nostrNotary.signRelayListMetadata(userId = userId, relays = relays).unwrapOrThrow()
        relaysSocketManager.publishEvent(nostrEvent = signedNostrEvent, relays = relays)
        importEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class, SignatureException::class)
    suspend fun publishGetBalanceRequest(nwcData: NostrWalletConnect) {
        val request = NwcWalletRequest(method = "get_balance", params = Unit)
        val balanceReqEvent = nostrNotary
            .signWalletBalanceRequestNostrEvent(
                request = request,
                nwc = nwcData,
            )
            .unwrapOrThrow()
        relaysSocketManager.publishNwcEvent(nostrEvent = balanceReqEvent)
    }
}

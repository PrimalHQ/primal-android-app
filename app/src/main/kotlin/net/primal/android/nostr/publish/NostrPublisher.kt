package net.primal.android.nostr.publish

import javax.inject.Inject
import net.primal.android.networking.primal.api.PrimalImportApi
import net.primal.android.networking.relays.RelaysSocketManager
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.nostr.notary.NostrReadOnlyMode
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.user.domain.Relay
import net.primal.android.wallet.nwc.model.LightningPayResponse
import timber.log.Timber

class NostrPublisher @Inject constructor(
    private val relaysSocketManager: RelaysSocketManager,
    private val nostrNotary: NostrNotary,
    private val primalImportApi: PrimalImportApi,
) {

    private suspend fun importEvent(event: NostrEvent): Boolean {
        val result = runCatching {
            primalImportApi.importEvents(events = listOf(event))
        }
        return result.isSuccess
    }

    @Throws(NostrPublishException::class, NostrReadOnlyMode::class)
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
                Timber.w("Failed to publish to outbox relays.", error)
            }
        }
        return importEvent(signedNostrEvent)
    }

    @Throws(NostrPublishException::class, NostrReadOnlyMode::class)
    suspend fun signPublishImportNostrEvent(
        userId: String,
        unsignedNostrEvent: NostrUnsignedEvent,
        outboxRelays: List<String> = emptyList(),
    ): PublishResult {
        val signedNostrEvent = nostrNotary.signNostrEvent(userId = userId, event = unsignedNostrEvent)
        val imported = publishAndImportEvent(signedNostrEvent = signedNostrEvent, outboxRelays = outboxRelays)
        return PublishResult(
            nostrEvent = signedNostrEvent,
            imported = imported,
        )
    }

    @Throws(NostrPublishException::class, NostrReadOnlyMode::class)
    suspend fun publishUserProfile(userId: String, contentMetadata: ContentMetadata): NostrEvent {
        val signedNostrEvent = nostrNotary.signMetadataNostrEvent(userId = userId, metadata = contentMetadata)
        relaysSocketManager.publishEvent(nostrEvent = signedNostrEvent)
        importEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class, NostrReadOnlyMode::class)
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

    @Throws(NostrPublishException::class, NostrReadOnlyMode::class)
    suspend fun setMuteList(userId: String, muteList: Set<String>): NostrEvent {
        val signedNostrEvent = nostrNotary.signMuteListNostrEvent(userId = userId, mutedUserIds = muteList)
        publishAndImportEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class, NostrReadOnlyMode::class)
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

    @Throws(NostrPublishException::class, NostrReadOnlyMode::class)
    suspend fun publishRelayList(userId: String, relays: List<Relay>): NostrEvent {
        val signedNostrEvent = nostrNotary.signRelayListMetadata(userId = userId, relays = relays)
        relaysSocketManager.publishEvent(nostrEvent = signedNostrEvent, relays = relays)
        importEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class, NostrReadOnlyMode::class)
    suspend fun publishWalletRequest(invoice: LightningPayResponse, nwcData: NostrWalletConnect) {
        val walletPayNostrEvent = nostrNotary.signWalletInvoiceRequestNostrEvent(
            request = invoice.toWalletPayRequest(),
            nwc = nwcData,
        )
        relaysSocketManager.publishNwcEvent(nostrEvent = walletPayNostrEvent)
    }
}

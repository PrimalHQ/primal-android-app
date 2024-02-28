package net.primal.android.nostr.publish

import javax.inject.Inject
import kotlinx.serialization.json.JsonArray
import net.primal.android.networking.primal.api.PrimalImportApi
import net.primal.android.networking.relays.RelaysSocketManager
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.nostr.notary.NostrNotary
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
        return try {
            primalImportApi.importEvents(events = listOf(event))
            true
        } catch (error: WssException) {
            Timber.w(error)
            false
        }
    }

    @Throws(NostrPublishException::class)
    suspend fun publishUserProfile(userId: String, contentMetadata: ContentMetadata): NostrEvent {
        val signedNostrEvent = nostrNotary.signMetadataNostrEvent(userId = userId, metadata = contentMetadata)
        relaysSocketManager.publishEvent(nostrEvent = signedNostrEvent)
        importEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class)
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
        relaysSocketManager.publishEvent(nostrEvent = signedNostrEvent)
        importEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class)
    suspend fun publishLikeNote(
        userId: String,
        postId: String,
        postAuthorId: String,
    ) {
        val signedNostrEvent = nostrNotary.signLikeReactionNostrEvent(
            userId = userId,
            postId = postId,
            postAuthorId = postAuthorId,
        )
        relaysSocketManager.publishEvent(signedNostrEvent)
        importEvent(signedNostrEvent)
    }

    @Throws(NostrPublishException::class)
    suspend fun publishRepostNote(
        userId: String,
        postId: String,
        postAuthorId: String,
        postRawNostrEvent: String,
    ) {
        val signedNostrEvent = nostrNotary.signRepostNostrEvent(
            userId = userId,
            postId = postId,
            postAuthorId = postAuthorId,
            postRawNostrEvent = postRawNostrEvent,
        )
        relaysSocketManager.publishEvent(nostrEvent = signedNostrEvent)
        importEvent(signedNostrEvent)
    }

    @Throws(NostrPublishException::class)
    suspend fun publishShortTextNote(
        userId: String,
        content: String,
        tags: Set<JsonArray> = emptySet(),
    ): Boolean {
        val noteEvent = nostrNotary.signShortTextNoteEvent(
            userId = userId,
            tags = tags.toList(),
            noteContent = content,
        )
        relaysSocketManager.publishEvent(nostrEvent = noteEvent)
        return importEvent(noteEvent)
    }

    @Throws(NostrPublishException::class)
    suspend fun setMuteList(userId: String, muteList: Set<String>): NostrEvent {
        val signedNostrEvent = nostrNotary.signMuteListNostrEvent(userId = userId, mutedUserIds = muteList)
        relaysSocketManager.publishEvent(signedNostrEvent)
        importEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class)
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
        relaysSocketManager.publishEvent(signedNostrEvent)
        importEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class)
    suspend fun publishRelayList(userId: String, relays: List<Relay>): NostrEvent {
        val signedNostrEvent = nostrNotary.signRelayListMetadata(userId = userId, relays = relays)
        relaysSocketManager.publishEvent(signedNostrEvent)
        importEvent(signedNostrEvent)
        return signedNostrEvent
    }

    @Throws(NostrPublishException::class)
    suspend fun publishWalletRequest(invoice: LightningPayResponse, nwcData: NostrWalletConnect) {
        val walletPayNostrEvent = nostrNotary.signWalletInvoiceRequestNostrEvent(
            request = invoice.toWalletPayRequest(),
            nwc = nwcData,
        )
        relaysSocketManager.publishNwcEvent(nostrEvent = walletPayNostrEvent)
    }
}

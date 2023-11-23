package net.primal.android.nostr.notary

import fr.acinq.secp256k1.Hex
import javax.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.toNostrRelayMap
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.toNpub
import net.primal.android.networking.UserAgentProvider
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asIdentifierTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.toTags
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.settings.api.model.AppSettingsDescription
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.NostrWallet
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.toZapTag
import net.primal.android.wallet.model.PayInvoiceRequest
import net.primal.android.wallet.model.WalletRequest
import net.primal.android.wallet.model.ZapTarget

class NostrNotary @Inject constructor(
    private val credentialsStore: CredentialsStore,
) {

    private val json = Json { ignoreUnknownKeys = true }

    private fun findNsecOrThrow(pubkey: String): String {
        return try {
            val npub = Hex.decode(pubkey).toNpub()
            credentialsStore.findOrThrow(npub = npub).nsec
        } catch (error: IllegalArgumentException) {
            throw NostrSignUnauthorized()
        }
    }

    fun signMetadataNostrEvent(
        userId: String,
        tags: List<JsonArray> = emptyList(),
        metadata: ContentMetadata,
    ): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.Metadata.value,
            tags = tags,
            content = json.encodeToString(metadata),
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signShortTextNoteEvent(
        userId: String,
        tags: List<JsonArray>,
        noteContent: String,
    ): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.ShortTextNote.value,
            tags = tags.toList(),
            content = noteContent,
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signAuthorizationNostrEvent(userId: String, description: String): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.ApplicationSpecificData.value,
            tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
            content = NostrJson.encodeToString(
                AppSettingsDescription(description = description),
            ),
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signAppSettingsNostrEvent(userId: String, appSettings: ContentAppSettings): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.ApplicationSpecificData.value,
            tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
            content = NostrJson.encodeToString(appSettings),
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signLikeReactionNostrEvent(
        userId: String,
        postId: String,
        postAuthorId: String,
    ): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.Reaction.value,
            tags = listOf(postId.asEventIdTag(), postAuthorId.asPubkeyTag()),
            content = "+",
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signRepostNostrEvent(
        userId: String,
        postId: String,
        postAuthorId: String,
        postRawNostrEvent: String,
    ): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.Reposts.value,
            tags = listOf(postId.asEventIdTag(), postAuthorId.asPubkeyTag()),
            content = postRawNostrEvent,
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signContactsNostrEvent(
        userId: String,
        contacts: Set<String>,
        relays: List<Relay>,
    ): NostrEvent {
        val tags = contacts.map { it.asPubkeyTag() }
        val content = NostrJson.encodeToString(relays.toNostrRelayMap())

        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.Contacts.value,
            content = content,
            tags = tags,
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signZapRequestNostrEvent(
        userId: String,
        comment: String,
        target: ZapTarget,
        relays: List<Relay>,
    ): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.ZapRequest.value,
            content = comment,
            tags = target.toTags() + listOf(relays.toZapTag()),
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signWalletInvoiceRequestNostrEvent(request: WalletRequest<PayInvoiceRequest>, nwc: NostrWallet): NostrEvent {
        val tags = listOf(nwc.pubkey.asPubkeyTag())
        val content = json.encodeToString(request)
        val encryptedMessage = CryptoUtils.encrypt(
            msg = content,
            privateKey = Hex.decode(nwc.keypair.privateKey),
            pubKey = Hex.decode(nwc.pubkey),
        )

        return NostrUnsignedEvent(
            pubKey = nwc.keypair.pubkey,
            kind = NostrEventKind.WalletRequest.value,
            content = encryptedMessage,
            tags = tags,
        ).signOrThrow(hexPrivateKey = Hex.decode(nwc.keypair.privateKey))
    }

    fun signImageUploadNostrEvent(userId: String, base64Content: String): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.PrimalImageUploadRequest.value,
            content = base64Content,
            tags = emptyList(),
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signMuteListNostrEvent(userId: String, mutedUserIds: Set<String>): NostrEvent {
        val tags = mutedUserIds.map { it.asPubkeyTag() }
        return NostrUnsignedEvent(
            content = "",
            pubKey = userId,
            kind = NostrEventKind.MuteList.value,
            tags = tags,
        ).signOrThrow(nsec = findNsecOrThrow(pubkey = userId))
    }

    fun signEncryptedDirectMessage(
        userId: String,
        receiverId: String,
        encryptedContent: String,
    ): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            content = encryptedContent,
            kind = NostrEventKind.EncryptedDirectMessages.value,
            tags = listOf(receiverId.asPubkeyTag()),
        ).signOrThrow(nsec = findNsecOrThrow(pubkey = userId))
    }
}

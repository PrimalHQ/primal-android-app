package net.primal.android.nostr.notary

import fr.acinq.secp256k1.Hex
import javax.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.NostrNotaryJson
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.toNpub
import net.primal.android.networking.UserAgentProvider
import net.primal.android.nostr.ext.asIdentifierTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.settings.api.model.AppSettingsDescription
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.toZapTag
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.domain.toTags
import net.primal.android.wallet.nwc.model.NwcWalletRequest
import net.primal.android.wallet.nwc.model.PayInvoiceRequest
import timber.log.Timber

class NostrNotary @Inject constructor(
    private val credentialsStore: CredentialsStore,
) {

    private fun findNsecOrThrow(pubkey: String): String {
        return try {
            val npub = Hex.decode(pubkey).toNpub()
            credentialsStore.findOrThrow(npub = npub).nsec
        } catch (error: IllegalArgumentException) {
            Timber.w(error)
            throw NostrSignUnauthorized()
        }
    }

    fun signNostrEvent(userId: String, event: NostrUnsignedEvent): NostrEvent {
        return event.signOrThrow(nsec = findNsecOrThrow(userId))
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
            content = NostrNotaryJson.encodeToString(metadata),
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

    fun signAppSpecificDataNostrEvent(userId: String, content: String): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.ApplicationSpecificData.value,
            tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
            content = content,
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signFollowListNostrEvent(
        userId: String,
        contacts: Set<String>,
        content: String,
    ): NostrEvent {
        val tags = contacts.map { it.asPubkeyTag() }
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.FollowList.value,
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

    fun signWalletInvoiceRequestNostrEvent(
        request: NwcWalletRequest<PayInvoiceRequest>,
        nwc: NostrWalletConnect,
    ): NostrEvent {
        val tags = listOf(nwc.pubkey.asPubkeyTag())
        val content = NostrNotaryJson.encodeToString(request)
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

    fun signImageUploadNostrEvent(userId: String, content: String): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.PrimalSimpleUploadRequest.value,
            content = content,
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

    fun signPrimalWalletOperationNostrEvent(userId: String, content: String): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            content = content,
            kind = NostrEventKind.PrimalWalletOperation.value,
            tags = listOf(),
        ).signOrThrow(nsec = findNsecOrThrow(pubkey = userId))
    }

    fun signRelayListMetadata(userId: String, relays: List<Relay>): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            content = "",
            kind = NostrEventKind.RelayListMetadata.value,
            tags = relays.map {
                buildJsonArray {
                    add("r")
                    add(it.url)
                    when {
                        it.read && it.write -> Unit
                        it.read -> add("read")
                        it.write -> add("write")
                    }
                }
            },
        ).signOrThrow(nsec = findNsecOrThrow(pubkey = userId))
    }
}

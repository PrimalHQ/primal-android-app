package net.primal.android.nostr.notary

import fr.acinq.secp256k1.Hex
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.hexToNsecHrp
import net.primal.android.crypto.toNpub
import net.primal.android.networking.UserAgentProvider
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asIdentifierTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.toTags
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.zap.PayInvoiceRequest
import net.primal.android.nostr.model.zap.WalletRequest
import net.primal.android.nostr.model.zap.ZapTarget
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.toNostrRelayMap
import net.primal.android.settings.api.model.AppSettingsDescription
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.NostrWallet
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.toZapTag
import javax.inject.Inject


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

    fun signAppSettingsSyncNostrEvent(
        userId: String
    ): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.ApplicationSpecificData.value,
            tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
            content = NostrJson.encodeToString(
                AppSettingsDescription(description = "Sync app settings")
            ),
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
        relays: List<Relay>
    ): NostrEvent {
        val tags = contacts.map { it.asPubkeyTag() }
        val content = NostrJson.encodeToString(relays.toNostrRelayMap())

        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.Contacts.value,
            content = content,
            tags = tags
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signZapRequestNostrEvent(
        userId: String,
        comment: String = "",
        target: ZapTarget,
        relays: List<Relay>
    ): NostrEvent {
        val tags = target.toTags()
            .toMutableList()
            .apply {
                add(relays.toZapTag())
            }

        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.ZapRequest.value,
            content = comment,
            tags = tags
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signWalletInvoiceRequestNostrEvent(
        request: WalletRequest<PayInvoiceRequest>,
        toPubkey: String,
        nwc: NostrWallet
    ): NostrEvent {
        val tags = listOf(buildJsonArray {
            add("p")
            add(toPubkey)
        })

        val content = json.encodeToString(request)
        val secret = CryptoUtils.getSharedSecret(nwc.secret.toByteArray(), toPubkey.toByteArray())
        val encryptedMessage = CryptoUtils.encrypt(content, secret)

        return NostrUnsignedEvent(
            pubKey = nwc.pubkey,
            kind = NostrEventKind.WalletRequest.value,
            content = encryptedMessage,
            tags = tags
        ).signOrThrow(nsec = nwc.secret.hexToNsecHrp())
    }
}

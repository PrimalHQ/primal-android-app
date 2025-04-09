package net.primal.android.nostr.notary

import android.content.ContentResolver
import fr.acinq.secp256k1.Hex
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.core.serialization.json.NostrNotaryJson
import net.primal.android.networking.UserAgentProvider
import net.primal.android.signer.signEventWithAmber
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.toZapTag
import net.primal.android.wallet.nwc.model.NwcWalletRequest
import net.primal.android.wallet.nwc.model.PayInvoiceRequest
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.api.settings.model.AppSettingsDescription
import net.primal.domain.ContentAppSettings
import net.primal.domain.nostr.ContentMetadata
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asIdentifierTag
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.cryptography.utils.CryptoUtils
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import net.primal.domain.nostr.cryptography.utils.toNpub
import net.primal.domain.nostr.zaps.ZapTarget
import net.primal.domain.nostr.zaps.toTags

class NostrNotary @Inject constructor(
    private val contentResolver: ContentResolver,
    private val credentialsStore: CredentialsStore,
) : NostrEventSignatureHandler {

    override fun signNostrEvent(unsignedNostrEvent: NostrUnsignedEvent): NostrEvent {
        return signNostrEvent(
            userId = unsignedNostrEvent.pubKey,
            event = unsignedNostrEvent,
        )
    }

    override fun verifySignature(nostrEvent: NostrEvent): Boolean {
        throw NotImplementedError()
    }

    private fun findNsecOrThrow(pubkey: String): String =
        runCatching {
            val npub = Hex.decode(pubkey).toNpub()
            credentialsStore.findOrThrow(npub = npub).nsec
        }.getOrNull() ?: throw SigningKeyNotFoundException()

    fun signNostrEvent(userId: String, event: NostrUnsignedEvent): NostrEvent {
        val isExternalSignerLogin = runCatching {
            credentialsStore.isExternalSignerLogin(npub = userId.hexToNpubHrp())
        }.getOrDefault(false)

        if (isExternalSignerLogin) {
            return contentResolver.signEventWithAmber(event = event) ?: throw SigningRejectedException()
        }

        return event.signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signMetadataNostrEvent(
        userId: String,
        tags: List<JsonArray> = emptyList(),
        metadata: ContentMetadata,
    ): NostrEvent {
        return signNostrEvent(
            userId = userId,
            event = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.Metadata.value,
                tags = tags,
                content = NostrNotaryJson.encodeToString(metadata),
            ),
        )
    }

    fun signAuthorizationNostrEvent(
        userId: String,
        description: String,
        tags: List<JsonArray> = emptyList(),
    ): NostrEvent {
        return signNostrEvent(
            userId = userId,
            event = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.ApplicationSpecificData.value,
                tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()) + tags,
                content = AppSettingsDescription(description = description).encodeToJsonString(),
            ),
        )
    }

    fun signAppSettingsNostrEvent(userId: String, appSettings: ContentAppSettings): NostrEvent {
        return signNostrEvent(
            userId = userId,
            event = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.ApplicationSpecificData.value,
                tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
                content = appSettings.encodeToJsonString(),
            ),
        )
    }

    fun signAppSpecificDataNostrEvent(userId: String, content: String): NostrEvent {
        return signNostrEvent(
            userId = userId,
            event = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.ApplicationSpecificData.value,
                tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
                content = content,
            ),
        )
    }

    fun signFollowListNostrEvent(
        userId: String,
        contacts: Set<String>,
        content: String,
    ): NostrEvent {
        val tags = contacts.map { it.asPubkeyTag() }
        return signNostrEvent(
            userId = userId,
            event = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.FollowList.value,
                content = content,
                tags = tags,
            ),
        )
    }

    fun signZapRequestNostrEvent(
        userId: String,
        comment: String,
        target: ZapTarget,
        relays: List<Relay>,
    ): NostrEvent {
        return signNostrEvent(
            userId = userId,
            event = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.ZapRequest.value,
                content = comment,
                tags = target.toTags() + listOf(relays.toZapTag()),
            ),
        )
    }

    @OptIn(ExperimentalEncodingApi::class)
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

        return signNostrEvent(
            userId = nwc.keypair.pubkey,
            event = NostrUnsignedEvent(
                pubKey = nwc.keypair.pubkey,
                kind = NostrEventKind.WalletRequest.value,
                content = encryptedMessage,
                tags = tags,
            ),
        )
    }

    fun signPrimalWalletOperationNostrEvent(userId: String, content: String): NostrEvent {
        return signNostrEvent(
            userId = userId,
            event = NostrUnsignedEvent(
                pubKey = userId,
                content = content,
                kind = NostrEventKind.PrimalWalletOperation.value,
                tags = listOf(),
            ),
        )
    }

    fun signRelayListMetadata(userId: String, relays: List<Relay>): NostrEvent {
        return signNostrEvent(
            userId = userId,
            event = NostrUnsignedEvent(
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
            ),
        )
    }
}

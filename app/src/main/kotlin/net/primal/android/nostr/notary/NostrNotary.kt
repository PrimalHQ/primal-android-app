package net.primal.android.nostr.notary

import android.content.ContentResolver
import fr.acinq.secp256k1.Hex
import javax.inject.Inject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.core.serialization.json.NostrNotaryJson
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.crypto.toNpub
import net.primal.android.networking.UserAgentProvider
import net.primal.android.nostr.ext.asIdentifierTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.notary.exceptions.MissingPrivateKey
import net.primal.android.nostr.notary.exceptions.NostrSignUnauthorized
import net.primal.android.signer.signEventWithAmber
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.toZapTag
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.domain.toTags
import net.primal.android.wallet.nwc.model.NwcWalletRequest
import net.primal.android.wallet.nwc.model.PayInvoiceRequest
import net.primal.core.utils.serialization.CommonJson
import net.primal.data.remote.api.settings.model.AppSettingsDescription
import net.primal.domain.ContentAppSettings
import net.primal.domain.nostr.ContentMetadata
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import timber.log.Timber

class NostrNotary @Inject constructor(
    private val contentResolver: ContentResolver,
    private val credentialsStore: CredentialsStore,
) {

    private fun findNsecOrThrow(pubkey: String): String {
        return try {
            val npub = Hex.decode(pubkey).toNpub()
            credentialsStore.findOrThrow(npub = npub).nsec ?: throw MissingPrivateKey()
        } catch (error: IllegalArgumentException) {
            Timber.w(error)
            throw NostrSignUnauthorized()
        }
    }

    fun signNostrEvent(userId: String, event: NostrUnsignedEvent): NostrEvent {
        val isExternalSignerLogin = runCatching {
            credentialsStore.isExternalSignerLogin(npub = userId.hexToNpubHrp())
        }.getOrDefault(false)

        if (isExternalSignerLogin) {
            return contentResolver.signEventWithAmber(event = event) ?: throw NostrSignUnauthorized()
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
                content = CommonJson.encodeToString(
                    AppSettingsDescription(description = description),
                ),
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
                content = CommonJson.encodeToString(appSettings),
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

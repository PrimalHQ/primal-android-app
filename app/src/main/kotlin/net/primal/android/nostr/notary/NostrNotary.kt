package net.primal.android.nostr.notary

import android.content.ContentResolver
import fr.acinq.secp256k1.Hex
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.core.serialization.json.NostrNotaryJson
import net.primal.android.networking.UserAgentProvider
import net.primal.android.signer.AmberSignResult
import net.primal.android.signer.signEventWithAmber
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.NostrWalletConnect
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.toZapTag
import net.primal.android.wallet.nwc.model.NwcWalletRequest
import net.primal.android.wallet.nwc.model.PayInvoiceRequest
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.api.settings.model.AppSettingsDescription
import net.primal.domain.global.ContentAppSettings
import net.primal.domain.nostr.ContentMetadata
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asIdentifierTag
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.SignResult
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.cryptography.utils.CryptoUtils
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import net.primal.domain.nostr.cryptography.utils.toNpub
import net.primal.domain.nostr.zaps.ZapTarget
import net.primal.domain.nostr.zaps.toTags

@Singleton
class NostrNotary @Inject constructor(
    dispatchers: DispatcherProvider,
    private val contentResolver: ContentResolver,
    private val credentialsStore: CredentialsStore,
) : NostrEventSignatureHandler {

    private val scope = CoroutineScope(dispatchers.main())
    private val _effects = Channel<NotarySideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: NotarySideEffect) = scope.launch { _effects.send(effect) }

    private val signMutex = Mutex()

    private val _responses = Channel<SignResult>()
    private fun setResponse(response: SignResult) = scope.launch { _responses.send(response) }

    override suspend fun signNostrEvent(unsignedNostrEvent: NostrUnsignedEvent): SignResult {
        val result = try {
            signNostrEvent(userId = unsignedNostrEvent.pubKey, event = unsignedNostrEvent)
        } catch (error: SignatureException) {
            return SignResult.Rejected(error)
        }

        return if (result != null) {
            SignResult.Signed(result)
        } else {
            signMutex.withLock {
                setEffect(NotarySideEffect.RequestSignature(unsignedNostrEvent))
                _responses.receive()
            }
        }
    }

    override fun verifySignature(nostrEvent: NostrEvent): Boolean {
        throw NotImplementedError()
    }

    fun onSuccess(nostrEvent: NostrEvent) {
        setResponse(SignResult.Signed(nostrEvent))
    }

    fun onFailure() {
        setResponse(SignResult.Rejected(SigningRejectedException()))
    }

    private fun findNsecOrThrow(pubkey: String): String =
        runCatching {
            val npub = Hex.decode(pubkey).toNpub()
            credentialsStore.findOrThrow(npub = npub).nsec
        }.getOrNull() ?: throw SigningKeyNotFoundException()

    private fun signNostrEvent(userId: String, event: NostrUnsignedEvent): NostrEvent? {
        val isExternalSignerLogin = runCatching {
            credentialsStore.isExternalSignerLogin(npub = userId.hexToNpubHrp())
        }.getOrDefault(false)

        if (isExternalSignerLogin) {
            val result = contentResolver.signEventWithAmber(event = event)
            return when (result) {
                AmberSignResult.Rejected -> throw SigningRejectedException()
                is AmberSignResult.Signed -> result.nostrEvent
                AmberSignResult.Undecided -> null
            }
        }

        return event.signOrThrow(nsec = findNsecOrThrow(userId))
    }

    suspend fun signMetadataNostrEvent(
        userId: String,
        tags: List<JsonArray> = emptyList(),
        metadata: ContentMetadata,
    ): SignResult {
        return signNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.Metadata.value,
                tags = tags,
                content = NostrNotaryJson.encodeToString(metadata),
            ),
        )
    }

    suspend fun signAuthorizationNostrEvent(
        userId: String,
        description: String,
        tags: List<JsonArray> = emptyList(),
    ): SignResult {
        return signNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.ApplicationSpecificData.value,
                tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()) + tags,
                content = AppSettingsDescription(description = description).encodeToJsonString(),
            ),
        )
    }

    suspend fun signAppSettingsNostrEvent(userId: String, appSettings: ContentAppSettings): SignResult {
        return signNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.ApplicationSpecificData.value,
                tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
                content = appSettings.encodeToJsonString(),
            ),
        )
    }

    suspend fun signAppSpecificDataNostrEvent(userId: String, content: String): SignResult {
        return signNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.ApplicationSpecificData.value,
                tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
                content = content,
            ),
        )
    }

    suspend fun signFollowListNostrEvent(
        userId: String,
        contacts: Set<String>,
        content: String,
    ): SignResult {
        val tags = contacts.map { it.asPubkeyTag() }
        return signNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.FollowList.value,
                content = content,
                tags = tags,
            ),
        )
    }

    suspend fun signZapRequestNostrEvent(
        userId: String,
        comment: String,
        target: ZapTarget,
        relays: List<Relay>,
    ): SignResult {
        return signNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.ZapRequest.value,
                content = comment,
                tags = target.toTags() + listOf(relays.toZapTag()),
            ),
        )
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun signWalletInvoiceRequestNostrEvent(
        request: NwcWalletRequest<PayInvoiceRequest>,
        nwc: NostrWalletConnect,
    ): SignResult {
        val tags = listOf(nwc.pubkey.asPubkeyTag())
        val content = NostrNotaryJson.encodeToString(request)
        val encryptedMessage = CryptoUtils.encrypt(
            msg = content,
            privateKey = Hex.decode(nwc.keypair.privateKey),
            pubKey = Hex.decode(nwc.pubkey),
        )

        return signNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
                pubKey = nwc.keypair.pubkey,
                kind = NostrEventKind.WalletRequest.value,
                content = encryptedMessage,
                tags = tags,
            ),
        )
    }

    suspend fun signPrimalWalletOperationNostrEvent(userId: String, content: String): SignResult {
        return signNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
                pubKey = userId,
                content = content,
                kind = NostrEventKind.PrimalWalletOperation.value,
                tags = listOf(),
            ),
        )
    }

    suspend fun signRelayListMetadata(userId: String, relays: List<Relay>): SignResult {
        return signNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
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

    sealed class NotarySideEffect {
        data class RequestSignature(val unsignedEvent: NostrUnsignedEvent) : NotarySideEffect()
    }
}

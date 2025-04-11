package net.primal.android.signer.poc

import android.content.ContentResolver
import dagger.hilt.android.scopes.ActivityRetainedScoped
import fr.acinq.secp256k1.Hex
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.android.nostr.notary.signOrThrow
import net.primal.android.signer.AmberSignResult
import net.primal.android.signer.signEventWithAmber
import net.primal.android.user.credentials.CredentialsStore
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import net.primal.domain.nostr.cryptography.utils.toNpub

@ActivityRetainedScoped
class NewNostrNotary @Inject constructor(
    dispatchers: DispatcherProvider,
    private val credentialsStore: CredentialsStore,
    private val contentResolver: ContentResolver,
) : Notary {

    private val scope = CoroutineScope(dispatchers.main())
    private val _effects = Channel<NotarySideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: NotarySideEffect) = scope.launch { _effects.send(effect) }

    private val signMutex = Mutex()

    private val _responses = Channel<SignResult>()
    private fun setResponse(response: SignResult) = scope.launch { _responses.send(response) }

    @Suppress("ReturnCount")
    override suspend fun signEvent(unsignedEvent: NostrUnsignedEvent): SignResult {
        val result = try {
            signNostrEvent(userId = unsignedEvent.pubKey, event = unsignedEvent)
        } catch (error: SignatureException) {
            return SignResult.Rejected(error)
        }

        if (result != null) {
            return SignResult.Signed(result)
        }

        signMutex.withLock {
            setEffect(NotarySideEffect.RequestSignature(unsignedEvent))
            return _responses.receive()
        }
    }

    fun onSuccess(nostrEvent: NostrEvent) {
        setResponse(SignResult.Signed(nostrEvent))
    }

    fun onFailure() {
        setResponse(SignResult.Rejected(SigningRejectedException()))
    }

    private fun signNostrEvent(userId: String, event: NostrUnsignedEvent): NostrEvent? {
        val isExternalSignerLogin = runCatching {
            credentialsStore.isExternalSignerLogin(npub = userId.hexToNpubHrp())
        }.getOrDefault(false)

        if (isExternalSignerLogin) {
            val result = contentResolver.signEventWithAmber(event = event)
            return when (result) {
                AmberSignResult.Rejected, AmberSignResult.Undecided -> throw SigningRejectedException()
                is AmberSignResult.Signed -> result.nostrEvent
            }
        }

        return event.signOrThrow(nsec = findNsecOrThrow(userId))
    }

    private fun findNsecOrThrow(pubkey: String): String =
        runCatching {
            val npub = Hex.decode(pubkey).toNpub()
            credentialsStore.findOrThrow(npub = npub).nsec
        }.getOrNull() ?: throw SigningKeyNotFoundException()
}

sealed class NotarySideEffect {
    data class RequestSignature(val unsignedEvent: NostrUnsignedEvent) : NotarySideEffect()
}

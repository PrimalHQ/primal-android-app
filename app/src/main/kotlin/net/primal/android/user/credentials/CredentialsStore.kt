package net.primal.android.user.credentials

import androidx.datastore.core.DataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import net.primal.android.user.domain.Credential
import net.primal.android.user.domain.CredentialType
import net.primal.android.user.domain.asCredential
import net.primal.domain.nostr.cryptography.utils.CryptoUtils
import net.primal.domain.nostr.cryptography.utils.bech32ToHexOrThrow
import net.primal.domain.nostr.cryptography.utils.extractKeyPairFromPrivateKeyOrThrow

@Singleton
class CredentialsStore @Inject constructor(
    private val persistence: DataStore<Set<Credential>>,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    val credentials = persistence.data
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = runBlocking { persistence.data.first() },
        )

    private suspend fun addCredential(credential: Credential) = persistence.updateData { it + credential }

    suspend fun clearCredentials() = persistence.updateData { emptySet() }

    fun isExternalSignerCredential(npub: String) =
        checkCredentialType(npub = npub, credentialType = CredentialType.ExternalSigner)

    fun isNpubCredential(npub: String) = checkCredentialType(npub = npub, credentialType = CredentialType.PublicKey)

    suspend fun getOrCreateInternalSignerCredentials() =
        credentials.value.find { it.type == CredentialType.InternalSigner }
            ?: CryptoUtils.generateHexEncodedKeypair()
                .asCredential(type = CredentialType.InternalSigner)
                .also { runCatching { addCredential(it) } }

    private fun checkCredentialType(npub: String, credentialType: CredentialType) =
        credentials.value.find { it.npub == npub }?.type == credentialType

    suspend fun saveNsec(nostrKey: String): String {
        val (nsec, pubkey) = nostrKey.extractKeyPairFromPrivateKeyOrThrow()
        addCredential(Credential(nsec = nsec, npub = pubkey, type = CredentialType.PrivateKey))
        return pubkey.bech32ToHexOrThrow()
    }

    suspend fun saveExternalSignerNpub(npub: String): String {
        addCredential(Credential(nsec = null, npub = npub, type = CredentialType.ExternalSigner))
        return npub.bech32ToHexOrThrow()
    }

    suspend fun saveNpub(npub: String): String {
        addCredential(Credential(nsec = null, npub = npub, type = CredentialType.PublicKey))
        return npub.bech32ToHexOrThrow()
    }

    suspend fun removeCredentialByNsec(nsec: String) =
        persistence.updateData {
            it.filterNot { cred -> cred.nsec == nsec }.toSet()
        }

    suspend fun removeCredentialByNpub(npub: String) =
        persistence.updateData {
            it.filterNot { cred -> cred.npub == npub }.toSet()
        }

    fun findOrThrow(npub: String): Credential =
        credentials.value.find { it.npub == npub }
            ?: throw IllegalArgumentException("Credential not found for $npub.")
}

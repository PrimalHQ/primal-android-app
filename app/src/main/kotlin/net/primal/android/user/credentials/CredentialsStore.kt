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
import net.primal.android.crypto.bech32ToHexOrThrow
import net.primal.android.crypto.extractKeyPairFromPrivateKeyOrThrow
import net.primal.android.user.domain.Credential

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

    fun isNpubLogin(npub: String): Boolean =
        credentials.value.find { it.npub == npub }?.nsec == null

    suspend fun saveNsec(nostrKey: String): String {
        val (nsec, pubkey) = nostrKey.extractKeyPairFromPrivateKeyOrThrow()
        addCredential(Credential(nsec = nsec, npub = pubkey))
        return pubkey.bech32ToHexOrThrow()
    }

    suspend fun saveNpub(npub: String): String {
        addCredential(Credential(nsec = null, npub = npub))
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

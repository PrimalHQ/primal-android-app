package net.primal.android.user.credentials

import androidx.datastore.core.DataStore
import net.primal.android.crypto.Bech32
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.toHex
import net.primal.android.crypto.toNpub
import net.primal.android.user.domain.Credential
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialsStore @Inject constructor(
    private val persistence: DataStore<List<Credential>>,
) {

    private suspend fun addCredential(credential: Credential) {
        persistence.updateData {
            it.toMutableList().apply { add(credential) }
        }
    }

    private suspend fun removeCredential(credential: Credential) {
        persistence.updateData {
            it.toMutableList().apply { remove(credential) }
        }
    }

    suspend fun clearCredentials() {
        persistence.updateData { emptyList() }
    }

    suspend fun save(nsec: String) : String {
        val decoded = Bech32.decodeBytes(nsec)
        val pubkey = CryptoUtils.publicKeyCreate(decoded.second)
        addCredential(Credential(nsec = nsec, npub = pubkey.toNpub()))
        return pubkey.toHex()
    }

}

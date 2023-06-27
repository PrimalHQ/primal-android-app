package net.primal.android.user.credentials

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
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

    private val scope = CoroutineScope(Dispatchers.IO)

    private val credentials = persistence.data
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = initialValue(),
        )

    private fun initialValue(): List<Credential> = runBlocking { persistence.data.first() }

    private suspend fun addCredential(credential: Credential) {
        persistence.updateData {
            it.toMutableList().apply { add(credential) }
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

    fun findOrThrow(npub: String): Credential = credentials.value.find { it.npub == npub }
        ?: throw IllegalArgumentException("Credential not found for $npub.")

}

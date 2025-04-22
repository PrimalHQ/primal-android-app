package net.primal.android.security

import androidx.datastore.core.DataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import net.primal.domain.nostr.cryptography.utils.CryptoUtils
import net.primal.domain.nostr.cryptography.utils.hexToNsecHrp

@Singleton
class PrimalDatabasePasswordProvider @Inject constructor(
    private val dataStore: DataStore<String>,
) {

    private fun readPassword(): String? =
        runBlocking {
            dataStore.data.firstOrNull()
        }

    private fun updatePassword(password: String) =
        runBlocking {
            dataStore.updateData { password }
        }

    fun providePassword(): String {
        return readPassword() ?: generateRandomPassword().apply {
            updatePassword(this)
        }
    }

    private fun generateRandomPassword() = CryptoUtils.generateHexEncodedKeypair().privateKey.hexToNsecHrp()
}

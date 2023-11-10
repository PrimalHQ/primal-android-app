package net.primal.android.security

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.toNsec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrimalDatabasePasswordProvider @Inject constructor(
    private val dataStore: DataStore<String>,
) {

    private fun generateRandomPassword() = CryptoUtils.privateKeyCreate()

    private fun readPassword(): String? = runBlocking {
        dataStore.data.firstOrNull()
    }

    private fun updatePassword(password: String) = runBlocking {
        dataStore.updateData { password }
    }

    fun providePassword(): String {
        return readPassword() ?: generateRandomPassword().toNsec().apply {
            updatePassword(this)
        }
    }

}

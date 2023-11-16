package net.primal.android.user.accounts

import androidx.datastore.core.DataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.android.user.domain.UserAccount

@Singleton
class UserAccountsStore @Inject constructor(
    private val persistence: DataStore<List<UserAccount>>,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val writeMutex = Mutex()

    val userAccounts = persistence.data
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = runBlocking { persistence.data.first() },
        )

    suspend fun getAndUpdateAccount(userId: String, reducer: UserAccount.() -> UserAccount): UserAccount =
        writeMutex.withLock {
            val current = findByIdOrNull(userId = userId) ?: UserAccount.buildLocal(pubkey = userId)
            val updated = current.reducer()
            updateOrInsertAccount(updated)
            updated
        }

    suspend fun upsertAccount(userAccount: UserAccount) =
        writeMutex.withLock {
            updateOrInsertAccount(userAccount)
        }

    private suspend fun updateOrInsertAccount(userAccount: UserAccount) {
        persistence.updateData { accounts ->
            val existingIndex = accounts.indexOfFirst { it.pubkey == userAccount.pubkey }
            accounts.toMutableList().apply {
                if (existingIndex != -1) removeAt(existingIndex)
                add(userAccount)
            }
        }
    }

    suspend fun deleteAccount(pubkey: String) =
        writeMutex.withLock {
            persistence.updateData { accounts ->
                val accountIndex = accounts.indexOfFirst { it.pubkey == pubkey }
                accounts.toMutableList().apply { removeAt(accountIndex) }
            }
        }

    suspend fun clearAllAccounts() =
        writeMutex.withLock {
            persistence.updateData { emptyList() }
        }

    fun findByIdOrNull(userId: String) = userAccounts.value.find { it.pubkey == userId }
}

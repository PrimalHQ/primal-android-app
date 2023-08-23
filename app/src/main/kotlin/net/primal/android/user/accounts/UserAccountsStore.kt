package net.primal.android.user.accounts

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import net.primal.android.user.domain.UserAccount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAccountsStore @Inject constructor(
    private val persistence: DataStore<List<UserAccount>>
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val userAccounts = persistence.data
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = runBlocking { persistence.data.first() },
        )

    suspend fun getAndUpdateAccount(
        userId: String,
        reducer: UserAccount.() -> UserAccount,
    ): UserAccount {
        val current = findByIdOrNull(userId = userId) ?: UserAccount.buildLocal(pubkey = userId)
        val updated = current.reducer()
        upsertAccount(updated)
        return updated
    }

    suspend fun upsertAccount(userAccount: UserAccount) {
        persistence.updateData { accounts ->
            val existingIndex = accounts.indexOfFirst { it.pubkey == userAccount.pubkey }
            accounts.toMutableList().apply {
                if (existingIndex != -1) removeAt(existingIndex)
                add(userAccount)
            }
        }
    }

    suspend fun deleteAccount(pubkey: String) {
        persistence.updateData { accounts ->
            val accountIndex = accounts.indexOfFirst { it.pubkey == pubkey }
            accounts.toMutableList().apply { removeAt(accountIndex) }
        }
    }

    suspend fun clearAllAccounts() {
        persistence.updateData { emptyList() }
    }

    fun findByIdOrNull(userId: String) = userAccounts.value.find { it.pubkey == userId }
}

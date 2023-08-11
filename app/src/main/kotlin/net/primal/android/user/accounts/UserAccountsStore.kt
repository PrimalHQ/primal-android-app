package net.primal.android.user.accounts

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import net.primal.android.user.domain.NostrWalletConnect
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

    fun findByIdOrNull(pubkey: String) = userAccounts.value.find { it.pubkey == pubkey }

    suspend fun setNostrWalletConnect(ownerId: String, nwc: NostrWalletConnect) {
        persistence.updateData { accounts ->
            val owner = accounts.find { it.pubkey === ownerId }

            if (owner != null) {
                val updated = owner.copy(nostrWalletConnect = nwc)
                accounts.toMutableList().replaceAll { updated }
            }

            accounts
        }
    }
}

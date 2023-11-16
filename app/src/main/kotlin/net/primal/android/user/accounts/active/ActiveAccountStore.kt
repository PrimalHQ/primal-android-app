package net.primal.android.user.accounts.active

import androidx.datastore.core.DataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.di.ActiveAccountDataStore
import net.primal.android.user.domain.UserAccount

@Singleton
class ActiveAccountStore @Inject constructor(
    accountsStore: UserAccountsStore,
    @ActiveAccountDataStore private val persistence: DataStore<String>,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val activeUserId = persistence.data.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = runBlocking { persistence.data.first() },
    )

    val activeUserAccount = accountsStore.userAccounts.map { it.findActiveAccountOrEmpty() }

    val activeAccountState = activeUserAccount.map { it.asActiveUserAccountState() }

    fun activeUserId() = activeUserId.value

    suspend fun activeUserAccount() = activeUserAccount.firstOrNull() ?: UserAccount.EMPTY

    private fun List<UserAccount>.findActiveAccountOrEmpty(): UserAccount {
        return this.find { it.pubkey == activeUserId() } ?: UserAccount.EMPTY
    }

    private fun UserAccount.asActiveUserAccountState(): ActiveUserAccountState =
        when (this) {
            UserAccount.EMPTY -> ActiveUserAccountState.NoUserAccount
            else -> ActiveUserAccountState.ActiveUserAccount(data = this)
        }

    fun setActiveUserId(pubkey: String) =
        runBlocking {
            persistence.updateData { pubkey }
        }

    suspend fun clearActiveUserAccount() {
        persistence.updateData { "" }
    }
}

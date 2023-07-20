package net.primal.android.user.active

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.di.ActiveAccountDataStore
import net.primal.android.user.domain.UserAccount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveAccountStore @Inject constructor(
    @ActiveAccountDataStore private val persistence: DataStore<String>,
    private val accountsStore: UserAccountsStore,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    val activeUserAccount = persistence.data
        .map { it.pubkeyToUserAccount() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = runBlocking { persistence.data.first().pubkeyToUserAccount() },
        )

    val activeAccountState = activeUserAccount.map { it.asActiveUserAccountState() }

    fun activeUserId() = activeUserAccount.value.pubkey

    private fun String.pubkeyToUserAccount(): UserAccount {
        return accountsStore.findByIdOrNull(pubkey = this) ?: UserAccount.EMPTY
    }

    private fun UserAccount.asActiveUserAccountState(): ActiveUserAccountState = when (this) {
        UserAccount.EMPTY -> ActiveUserAccountState.NoUserAccount
        else -> ActiveUserAccountState.ActiveUserAccount(data = this)
    }

    fun setActiveUserId(pubkey: String) = runBlocking {
        persistence.updateData { pubkey }
    }

    suspend fun clearActiveUserAccount() {
        persistence.updateData { "" }
    }

}

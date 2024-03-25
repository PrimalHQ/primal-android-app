package net.primal.android.user.accounts.active

import androidx.datastore.core.DataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.di.ActiveAccountDataStore
import net.primal.android.user.domain.UserAccount

@Singleton
class ActiveAccountStore @Inject constructor(
    accountsStore: UserAccountsStore,
    dispatchers: CoroutineDispatcherProvider,
    @ActiveAccountDataStore private val persistence: DataStore<String>,
) {

    private val scope = CoroutineScope(dispatchers.io())

    val activeUserId = persistence.data.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = runBlocking { persistence.data.first() },
    )

    val activeUserAccount = accountsStore.userAccounts
        .map { it.findActiveAccountOrEmpty() }
        .distinctUntilChanged()

    val activeAccountState = activeUserAccount
        .map { it.asActiveUserAccountState() }
        .distinctUntilChanged()

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

    suspend fun setActiveUserId(pubkey: String) {
        persistence.updateData { pubkey }
    }

    suspend fun clearActiveUserAccount() {
        persistence.updateData { "" }
    }
}

package net.primal.android.user.active

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import net.primal.android.user.domain.UserAccount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveAccountStore @Inject constructor(
    private val persistence: DataStore<UserAccount>,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    val activeAccountState = persistence.data
        .map { it.asActiveUserAccountState() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = initialValue(),
        )

    private fun initialValue(): ActiveUserAccountState {
        val activeCompany = runBlocking { persistence.data.first() }
        return activeCompany.asActiveUserAccountState()
    }

    private fun UserAccount.asActiveUserAccountState(): ActiveUserAccountState = when (this) {
        UserAccount.EMPTY -> ActiveUserAccountState.NoUserAccount
        else -> ActiveUserAccountState.ActiveUserAccount(data = this)
    }

    fun setActiveUserAccount(userAccount: UserAccount) = runBlocking {
        persistence.updateData { userAccount }
    }

    suspend fun clearActiveUserAccount() {
        persistence.updateData { UserAccount.EMPTY }
    }

}

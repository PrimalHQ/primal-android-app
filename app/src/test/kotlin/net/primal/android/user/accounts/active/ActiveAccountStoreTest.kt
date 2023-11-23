package net.primal.android.user.accounts.active

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import net.primal.android.security.NoEncryption
import net.primal.android.core.serialization.datastore.StringSerializer
import net.primal.android.user.accounts.UserAccountsSerialization
import net.primal.android.test.MainDispatcherRule
import net.primal.android.test.advanceUntilIdleAndDelay
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.domain.UserAccount
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ActiveAccountStoreTest {

    companion object {
        private const val DATA_STORE_FILE = "testActiveAccount.json"
        private const val DATA_ACCOUNTS_FILE = "testAccounts.json"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val persistenceActiveAccount: DataStore<String> = DataStoreFactory.create(
        serializer = StringSerializer(),
        produceFile = { testContext.dataStoreFile(DATA_STORE_FILE) }
    )

    private val persistenceAccounts: DataStore<List<UserAccount>> = DataStoreFactory.create(
        serializer = UserAccountsSerialization(encryption = NoEncryption()),
        produceFile = { testContext.dataStoreFile(DATA_ACCOUNTS_FILE) }
    )

    private val expectedPubkey = "pubkey"

    private fun buildUserAccount(
        pubkey: String = expectedPubkey,
        authorDisplayName: String = "Alex",
        userDisplayName: String = "alex",
    ): UserAccount {
        return UserAccount(
            pubkey = pubkey,
            authorDisplayName = authorDisplayName,
            userDisplayName = userDisplayName,
        )
    }

    private suspend fun fakeAccountsStore(
        accounts: List<UserAccount> = emptyList()
    ) = UserAccountsStore(persistence = persistenceAccounts).apply {
        accounts.forEach { upsertAccount(it) }
    }

    @Test
    fun `initial activeAccountState is NoUserAccount`() = runTest {
        val activeAccountStore = ActiveAccountStore(
            persistence = persistenceActiveAccount,
            accountsStore = fakeAccountsStore()
        )

        val actual = activeAccountStore.activeAccountState.firstOrNull()
        actual shouldBe ActiveUserAccountState.NoUserAccount
    }

    @Test
    fun `initial userAccount is empty UserAccount`() = runTest {
        val activeAccountStore = ActiveAccountStore(
            persistence = persistenceActiveAccount,
            accountsStore = fakeAccountsStore()
        )

        val actual = activeAccountStore.activeUserAccount()
        actual shouldBe UserAccount.EMPTY
    }

    @Test
    fun `setActiveUserId stores active account to data store`() = runTest {
        val expectedUserAccount = buildUserAccount()
        val activeAccountStore = ActiveAccountStore(
            persistence = persistenceActiveAccount,
            accountsStore = fakeAccountsStore(
                accounts = listOf(expectedUserAccount)
            )
        )

        activeAccountStore.setActiveUserId(expectedPubkey)
        advanceUntilIdleAndDelay()

        val actual = activeAccountStore.activeUserAccount()
        actual shouldBe expectedUserAccount
    }

    @Test
    fun `activeUserId returns user pubkey of the active user`() = runTest {
        val expectedUserAccount = buildUserAccount()
        val activeAccountStore = ActiveAccountStore(
            persistence = persistenceActiveAccount,
            accountsStore = fakeAccountsStore(
                accounts = listOf(expectedUserAccount)
            )
        )

        activeAccountStore.setActiveUserId(expectedPubkey)
        advanceUntilIdleAndDelay()

        val actual = activeAccountStore.activeUserId()
        actual shouldBe expectedPubkey
    }

    @Test
    fun `clearActiveUserAccount reverts to empty UserAccount`() = runTest {
        persistenceActiveAccount.updateData { expectedPubkey }
        val activeAccountStore = ActiveAccountStore(
            persistence = persistenceActiveAccount,
            accountsStore = fakeAccountsStore()
        )

        activeAccountStore.clearActiveUserAccount()
        advanceUntilIdleAndDelay()

        val actual = activeAccountStore.activeUserAccount()
        actual shouldBe UserAccount.EMPTY
    }

    @Test
    fun `clearActiveUserAccount reverts to NoUserAccount state`() = runTest {
        persistenceActiveAccount.updateData { expectedPubkey }
        val activeAccountStore = ActiveAccountStore(
            persistence = persistenceActiveAccount,
            accountsStore = fakeAccountsStore(
                accounts = listOf(buildUserAccount())
            )
        )

        activeAccountStore.clearActiveUserAccount()
        advanceUntilIdleAndDelay()

        val actual = activeAccountStore.activeAccountState.firstOrNull()
        actual shouldBe ActiveUserAccountState.NoUserAccount
    }

    @Test
    fun `updating account data triggers active account data to be updated`() = runTest {
        val existingAccount = buildUserAccount(
            pubkey = expectedPubkey,
            authorDisplayName = "alex"
        )
        persistenceActiveAccount.updateData { existingAccount.pubkey }
        val accountsStore = fakeAccountsStore(accounts = listOf(existingAccount))
        val activeAccountStore = ActiveAccountStore(
            persistence = persistenceActiveAccount,
            accountsStore = accountsStore,
        )

        accountsStore.upsertAccount(existingAccount.copy(authorDisplayName = "nikola"))
        advanceUntilIdleAndDelay()

        val updatedAccount = activeAccountStore.activeAccountState.firstOrNull()
        updatedAccount.shouldBeInstanceOf<ActiveUserAccountState.ActiveUserAccount>()
        updatedAccount.data.authorDisplayName shouldBe "nikola"
    }
}

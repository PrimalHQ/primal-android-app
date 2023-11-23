package net.primal.android.user.accounts

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.primal.android.security.NoEncryption
import net.primal.android.test.MainDispatcherRule
import net.primal.android.test.advanceUntilIdleAndDelay
import net.primal.android.user.domain.UserAccount
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class UserAccountsStoreTest {

    companion object {
        private const val DATA_STORE_FILE = "testAccounts.json"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val persistence: DataStore<List<UserAccount>> = DataStoreFactory.create(
        serializer = UserAccountsSerialization(encryption = NoEncryption()),
        produceFile = { testContext.dataStoreFile(DATA_STORE_FILE) }
    )

    private val expectedPubkey = "pubkey"

    private fun buildUserAccount(
        expectedUserId: String = expectedPubkey,
        expectedAuthorDisplayName: String = "Alex",
        expectedUserDisplayName: String = "alex",
    ): UserAccount {
        return UserAccount(
            pubkey = expectedUserId,
            authorDisplayName = expectedAuthorDisplayName,
            userDisplayName = expectedUserDisplayName,
        )
    }

    @Test
    fun `initial accounts are empty`() = runTest {
        val accountsStore = UserAccountsStore(persistence)
        val actual = accountsStore.userAccounts.value
        actual.shouldBeEmpty()
    }

    @Test
    fun `upsertAccount inserts given account to data store`() = runTest {
        val expectedAccount = buildUserAccount()
        val accountsStore = UserAccountsStore(persistence)

        accountsStore.upsertAccount(expectedAccount)
        advanceUntilIdleAndDelay()

        val accounts = accountsStore.userAccounts.value
        val actual = accounts.find { it.pubkey == expectedPubkey }

        actual shouldBe expectedAccount
    }

    @Test
    fun `upsertAccount updates account in data store`() = runTest {
        val existingAccount = buildUserAccount(expectedAuthorDisplayName = "alex")
        persistence.updateData { it.toMutableList().apply { add((existingAccount)) } }
        val accountsStore = UserAccountsStore(persistence)

        val expectedAccount = buildUserAccount(expectedAuthorDisplayName = "updated")
        accountsStore.upsertAccount(expectedAccount)
        advanceUntilIdleAndDelay()

        val accounts = accountsStore.userAccounts.value
        val actual = accounts.find { it.pubkey == expectedPubkey }

        actual shouldBe expectedAccount
    }

    @Test
    fun `deleteAccount deletes given account from data store`() = runTest {
        val existingAccount = buildUserAccount()
        persistence.updateData { it.toMutableList().apply { add((existingAccount)) } }
        val accountsStore = UserAccountsStore(persistence)

        accountsStore.deleteAccount(pubkey = expectedPubkey)
        advanceUntilIdleAndDelay()

        val accounts = accountsStore.userAccounts.value
        val actual = accounts.find { it.pubkey == expectedPubkey }

        actual.shouldBeNull()
    }

    @Test
    fun `clearAllAccounts deletes all accounts from data store`() = runTest {
        val existingAccount = buildUserAccount()
        persistence.updateData { it.toMutableList().apply { add((existingAccount)) } }
        val accountsStore = UserAccountsStore(persistence)

        accountsStore.clearAllAccounts()
        advanceUntilIdleAndDelay()

        val actual = accountsStore.userAccounts.value
        actual.shouldBeEmpty()
    }

    @Test
    fun `findByIdOrNull finds account by id`() = runTest {
        val existingAccount = buildUserAccount()
        persistence.updateData { it.toMutableList().apply { add((existingAccount)) } }
        val accountsStore = UserAccountsStore(persistence)

        val actual = accountsStore.findByIdOrNull(userId = expectedPubkey)
        actual.shouldNotBeNull()
        actual shouldBe existingAccount
    }

    @Test
    fun `findByIdOrNull returns null for not found id`() {
        val accountsStore = UserAccountsStore(persistence)
        val actual = accountsStore.findByIdOrNull(userId = "nonExisting")
        actual.shouldBeNull()
    }

}

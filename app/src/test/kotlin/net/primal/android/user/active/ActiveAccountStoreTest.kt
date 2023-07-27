package net.primal.android.user.active

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import net.primal.android.serialization.StringSerializer
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
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val persistence: DataStore<String> = DataStoreFactory.create(
        serializer = StringSerializer(),
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

    private fun mockkAccountsStore(
        findByIdResult: UserAccount = UserAccount.EMPTY,
    ) = mockk<UserAccountsStore>(relaxed = true) {
        every { findByIdOrNull(any()) } returns findByIdResult
    }

    @Test
    fun `initial activeAccountState is NoUserAccount`() = runTest {
        val activeAccountStore = ActiveAccountStore(
            persistence = persistence,
            accountsStore = mockkAccountsStore()
        )

        val actual = activeAccountStore.activeAccountState.firstOrNull()
        actual shouldBe ActiveUserAccountState.NoUserAccount
    }

    @Test
    fun `initial userAccount is empty UserAccount`() {
        val activeAccountStore = ActiveAccountStore(
            persistence = persistence,
            accountsStore = mockkAccountsStore()
        )

        val actual = activeAccountStore.activeUserAccount.value
        actual shouldBe UserAccount.EMPTY
    }

    @Test
    fun `setActiveUserId stores active account to data store`() = runTest {
        val expectedUserAccount = buildUserAccount()
        val activeAccountStore = ActiveAccountStore(
            persistence = persistence,
            accountsStore = mockkAccountsStore(findByIdResult = expectedUserAccount)
        )

        activeAccountStore.setActiveUserId(expectedPubkey)
        advanceUntilIdleAndDelay()

        val actual = activeAccountStore.activeUserAccount.value
        actual shouldBe expectedUserAccount
    }

    @Test
    fun `activeUserId returns user pubkey of the active user`() = runTest {
        val expectedUserAccount = buildUserAccount()
        val activeAccountStore = ActiveAccountStore(
            persistence = persistence,
            accountsStore = mockkAccountsStore(findByIdResult = expectedUserAccount)
        )

        activeAccountStore.setActiveUserId(expectedPubkey)
        advanceUntilIdleAndDelay()

        val actual = activeAccountStore.activeUserId()
        actual shouldBe expectedPubkey
    }

    @Test
    fun `clearActiveUserAccount reverts to empty UserAccount`() = runTest {
        persistence.updateData { expectedPubkey }
        val activeAccountStore = ActiveAccountStore(
            persistence = persistence,
            accountsStore = mockkAccountsStore()
        )

        activeAccountStore.clearActiveUserAccount()
        advanceUntilIdleAndDelay()

        val actual = activeAccountStore.activeUserAccount.value
        actual shouldBe UserAccount.EMPTY
    }

    @Test
    fun `clearActiveUserAccount reverts to NoUserAccount state`() = runTest {
        persistence.updateData { expectedPubkey }
        val activeAccountStore = ActiveAccountStore(
            persistence = persistence,
            accountsStore = mockkAccountsStore()
        )

        activeAccountStore.clearActiveUserAccount()
        advanceUntilIdleAndDelay()

        val actual = activeAccountStore.activeAccountState.firstOrNull()
        actual shouldBe ActiveUserAccountState.NoUserAccount
    }

}

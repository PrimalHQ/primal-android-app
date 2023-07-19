package net.primal.android.user.active

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import net.primal.android.security.NoEncryption
import net.primal.android.serialization.UserAccountSerialization
import net.primal.android.test.MainDispatcherRule
import net.primal.android.test.advanceUntilIdleAndDelay
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

    private val persistence: DataStore<UserAccount> = DataStoreFactory.create(
        serializer = UserAccountSerialization(encryption = NoEncryption()),
        produceFile = { testContext.dataStoreFile(DATA_STORE_FILE) }
    )

    @Test
    fun `initial activeAccountState is NoUserAccount`() = runTest {
        val activeAccountStore = ActiveAccountStore(persistence = persistence)

        val actual = activeAccountStore.activeAccountState.firstOrNull()
        actual shouldBe ActiveUserAccountState.NoUserAccount
    }

    @Test
    fun `initial userAccount is empty UserAccount`() {
        val activeAccountStore = ActiveAccountStore(persistence = persistence)

        val actual = activeAccountStore.userAccount.value
        actual shouldBe UserAccount.EMPTY
    }

    @Test
    fun `setActiveUserAccount stores active account to data store`() = runTest {
        val expectedUserAccount = UserAccount(pubkey = "pubkey", displayName = "alex")
        val activeAccountStore = ActiveAccountStore(persistence = persistence)

        activeAccountStore.setActiveUserAccount(expectedUserAccount)
        advanceUntilIdleAndDelay()

        val actual = activeAccountStore.userAccount.value
        actual shouldBe expectedUserAccount
    }


    @Test
    fun `clearActiveUserAccount reverts to empty UserAccount`() = runTest {
        val activeUserAccount = UserAccount(pubkey = "pubkey", displayName = "alex")
        persistence.updateData { activeUserAccount }
        val activeAccountStore = ActiveAccountStore(persistence = persistence)

        activeAccountStore.clearActiveUserAccount()
        advanceUntilIdleAndDelay()

        val actual = activeAccountStore.userAccount.value
        actual shouldBe UserAccount.EMPTY
    }

    @Test
    fun `clearActiveUserAccount reverts to NoUserAccount state`() = runTest {
        val activeUserAccount = UserAccount(pubkey = "pubkey", displayName = "alex")
        persistence.updateData { activeUserAccount }
        val activeAccountStore = ActiveAccountStore(persistence = persistence)

        activeAccountStore.clearActiveUserAccount()
        advanceUntilIdleAndDelay()

        val actual = activeAccountStore.activeAccountState.firstOrNull()
        actual shouldBe ActiveUserAccountState.NoUserAccount
    }

}

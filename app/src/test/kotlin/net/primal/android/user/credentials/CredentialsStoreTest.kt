package net.primal.android.user.credentials

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.primal.android.crypto.Bech32
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.toHex
import net.primal.android.crypto.toNpub
import net.primal.android.security.NoEncryption
import net.primal.android.test.MainDispatcherRule
import net.primal.android.test.advanceUntilIdleAndDelay
import net.primal.android.user.domain.Credential
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CredentialsStoreTest {

    companion object {
        private const val DATA_STORE_FILE = "testCredentials.json"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expectedNsec = "nsec1es53y8wn6dcjfxdynetuh6apxjwpttl79xjrl7rtkwnampaqtctq02lrv5"

    private val expectedCredential by lazy {
        val decoded = Bech32.decodeBytes(expectedNsec)
        val pubkey = CryptoUtils.publicKeyCreate(decoded.second)
        Credential(nsec = expectedNsec, npub = pubkey.toNpub())
    }

    private val testContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val persistence: DataStore<List<Credential>> = DataStoreFactory.create(
        serializer = CredentialsSerialization(encryption = NoEncryption()),
        produceFile = { testContext.dataStoreFile(DATA_STORE_FILE) }
    )

    @Test
    fun `saving invalid input should throw InvalidNostrKeyException`() = runTest {
        val credentialsStore = CredentialsStore(persistence = persistence)
        shouldThrow<CredentialsStore.InvalidNostrKeyException> {
            credentialsStore.save(nostrKey = "invalid nsec")
        }
    }

    @Test
    fun `save stores nsec to data store as Credential`() = runTest {
        val credentialsStore = CredentialsStore(persistence = persistence)
        credentialsStore.save(nostrKey = expectedNsec)
        advanceUntilIdleAndDelay()

        val actual = credentialsStore.credentials.value
        actual.size shouldBe 1
        actual.first() shouldBeEqual expectedCredential
    }

    @Test
    fun `save returns pubkey in hex value`() = runTest {
        val credentialsStore = CredentialsStore(persistence = persistence)
        val actual = credentialsStore.save(nostrKey = expectedNsec)
        actual shouldBe Bech32.decodeBytes(expectedCredential.npub).second.toHex()
    }

    @Test
    fun `findOrThrow finds credential by given npub`() = runTest {
        persistence.updateData {
            listOf(
                expectedCredential,
                Credential(nsec = "invalidNsec", npub = "invalidNpub")
            )
        }
        val credentialsStore = CredentialsStore(persistence = persistence)

        val actual = credentialsStore.findOrThrow(expectedCredential.npub)
        actual shouldBe expectedCredential
    }

    @Test
    fun `findOrThrow throws IllegalArgumentException if credential is not found`() {
        val credentialsStore = CredentialsStore(persistence = persistence)
        shouldThrow<IllegalArgumentException> {
            credentialsStore.findOrThrow(npub = "missing npub")
        }
    }

    @Test
    fun `clearCredentials removes all credentials from data store`() = runTest {
        persistence.updateData { listOf(expectedCredential) }
        val credentialsStore = CredentialsStore(persistence = persistence)

        credentialsStore.clearCredentials()
        advanceUntilIdleAndDelay()

        val actual = credentialsStore.credentials.value
        actual shouldBe emptyList()
    }

}

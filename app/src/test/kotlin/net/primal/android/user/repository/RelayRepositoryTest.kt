package net.primal.android.user.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.user.domain.RelayKind
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class RelayRepositoryTest {

    private lateinit var myDatabase: PrimalDatabase

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        myDatabase = Room.inMemoryDatabaseBuilder(context, PrimalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        myDatabase.close()
    }

    @Test
    fun bootstrapDefaultUserRelays_replaceUserRelaysInDatabase() = runTest {
        val userId = "random"
        val expectedRelays = listOf("wss://relay.primal.net", "wss://relay.damus.io")
        val nostrPublisher = NostrPublisher(
            relaysSocketManager = mockk(relaxed = true),
            nostrNotary = mockk(relaxed = true),
            primalImportApi = mockk(relaxed = true),
        )
        val repository = RelayRepository(
            nostrPublisher = nostrPublisher,
            usersApi = mockk(relaxed = true) {
                coEvery { getDefaultRelays() } returns expectedRelays
            },
            primalDatabase = myDatabase,
        )

        repository.bootstrapUserRelays(userId = userId)

        val actualRelays = myDatabase.relays().findRelays(userId = userId, kind = RelayKind.UserRelay)
        actualRelays.map { it.url }.sorted() shouldBe expectedRelays.sorted()
    }
}

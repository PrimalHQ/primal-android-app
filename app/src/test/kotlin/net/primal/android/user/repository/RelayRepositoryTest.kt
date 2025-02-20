package net.primal.android.user.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.user.api.model.UsersRelaysResponse
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.RelayKind
import net.primal.android.user.domain.cleanWebSocketUrl
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

    private fun buildPrimalUserRelaysListEvent(relays: List<String>): PrimalEvent {
        return PrimalEvent(
            kind = NostrEventKind.PrimalUserRelaysList.value,
            tags = mutableListOf<JsonArray>().apply {
                relays.forEach {
                    add(
                        buildJsonArray {
                            add("r")
                            add(it)
                        },
                    )
                }
            },
        )
    }

    @Test
    fun bootstrapDefaultUserRelays_replaceUserRelaysInDatabase() =
        runTest {
            val userId = "random"
            val expectedRelays = listOf("wss://relay.primal.net", "wss://relay.damus.io")
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val repository = RelayRepository(
                nostrPublisher = nostrPublisher,
                usersApi = mockk(relaxed = true) {
                    coEvery { getDefaultRelays() } returns expectedRelays
                },
                primalDatabase = myDatabase,
                dispatchers = coroutinesTestRule.dispatcherProvider,
            )

            repository.bootstrapUserRelays(userId = userId)

            val actualRelays = myDatabase.relays().findRelays(userId = userId, kind = RelayKind.UserRelay)
            actualRelays.map { it.url }.sorted() shouldBe expectedRelays.sorted()
        }

    @Test
    fun removeRelayAndPublishRelayList_removesRelayFromDatabase() =
        runTest {
            val userId = "random"
            val relays = listOf("wss://relay.primal.net", "wss://nostr1.current.fyi/")
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val repository = RelayRepository(
                nostrPublisher = nostrPublisher,
                usersApi = mockk(relaxed = true) {
                    coEvery { getUserRelays(listOf(userId)) } returns UsersRelaysResponse(
                        cachedRelayListEvents = listOf(buildPrimalUserRelaysListEvent(relays = relays)),
                    )
                },
                primalDatabase = myDatabase,
                dispatchers = coroutinesTestRule.dispatcherProvider,
            )

            repository.removeRelayAndPublishRelayList(userId = userId, url = relays.first())
            val expectedRelays = relays.drop(1).map { it.cleanWebSocketUrl() }.sorted()

            val actualRelays = myDatabase.relays().findRelays(userId = userId, kind = RelayKind.UserRelay)
            actualRelays.map { it.url }.sorted() shouldBe expectedRelays
        }

    @Test
    fun removeRelayAndPublishRelayList_removesEvenIfRelayUrlIsNotCleaned() =
        runTest {
            val userId = "random"
            val relays = listOf("wss://nostr1.current.fyi", "wss://relay.primal.net")
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val repository = RelayRepository(
                nostrPublisher = nostrPublisher,
                usersApi = mockk(relaxed = true) {
                    coEvery { getUserRelays(listOf(userId)) } returns UsersRelaysResponse(
                        cachedRelayListEvents = listOf(buildPrimalUserRelaysListEvent(relays = relays)),
                    )
                },
                primalDatabase = myDatabase,
                dispatchers = coroutinesTestRule.dispatcherProvider,
            )

            repository.removeRelayAndPublishRelayList(userId = userId, url = "wss://nostr1.current.fyi/")
            val expectedRelays = relays.drop(1).map { it.cleanWebSocketUrl() }.sorted()

            val actualRelays = myDatabase.relays().findRelays(userId = userId, kind = RelayKind.UserRelay)
            actualRelays.map { it.url }.sorted() shouldBe expectedRelays
        }

    @Test
    fun removeRelayAndPublishRelayList_callsPublishWithNewRelayList() =
        runTest {
            val userId = "random"
            val relays = listOf("wss://relay.primal.net", "wss://nostr1.current.fyi/")
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val repository = RelayRepository(
                nostrPublisher = nostrPublisher,
                usersApi = mockk(relaxed = true) {
                    coEvery { getUserRelays(listOf(userId)) } returns UsersRelaysResponse(
                        cachedRelayListEvents = listOf(buildPrimalUserRelaysListEvent(relays = relays)),
                    )
                },
                primalDatabase = myDatabase,
                dispatchers = coroutinesTestRule.dispatcherProvider,
            )

            repository.removeRelayAndPublishRelayList(userId = userId, url = relays.first())
            val expectedRelays = relays.drop(1).map { Relay(url = it, read = true, write = true) }

            coVerify {
                nostrPublisher.publishRelayList(
                    withArg { it shouldBe userId },
                    withArg { it shouldBe expectedRelays },
                )
            }
        }

    @Test
    fun fetchAndUpdateUserRelays_clearsUserRelaysIfCachedNip65HasEmptyTags() =
        runTest {
            val userId = "random"
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val repository = RelayRepository(
                nostrPublisher = nostrPublisher,
                usersApi = mockk(relaxed = true) {
                    coEvery { getUserRelays(listOf(userId)) } returns UsersRelaysResponse(
                        cachedRelayListEvents = listOf(buildPrimalUserRelaysListEvent(relays = emptyList())),
                    )
                },
                primalDatabase = myDatabase,
                dispatchers = coroutinesTestRule.dispatcherProvider,
            )

            myDatabase.relays().upsertAll(
                relays = listOf(
                    net.primal.android.user.db.Relay(
                        userId = userId,
                        kind = RelayKind.UserRelay,
                        read = true,
                        url = "wss://relay.primal.net",
                        write = true,
                    ),
                ),
            )

            repository.fetchAndUpdateUserRelays(userId = userId)

            val actualRelays = myDatabase.relays().findRelays(userId = userId, kind = RelayKind.UserRelay)
            actualRelays shouldBe emptyList()
        }

    @Test
    fun fetchAndUpdateUserRelays_ignoresFetchIfCachedNip65IsMissing() =
        runTest {
            val userId = "random"
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val repository = RelayRepository(
                nostrPublisher = nostrPublisher,
                usersApi = mockk(relaxed = true) {
                    coEvery { getUserRelays(listOf(userId)) } returns UsersRelaysResponse(cachedRelayListEvents = emptyList())
                },
                primalDatabase = myDatabase,
                dispatchers = coroutinesTestRule.dispatcherProvider,
            )

            val expectedRelays = listOf(
                net.primal.android.user.db.Relay(
                    userId = userId,
                    kind = RelayKind.UserRelay,
                    read = true,
                    url = "wss://relay.primal.net",
                    write = true,
                ),
            )
            myDatabase.relays().upsertAll(relays = expectedRelays)

            repository.fetchAndUpdateUserRelays(userId = userId)

            val actualRelays = myDatabase.relays().findRelays(userId = userId, kind = RelayKind.UserRelay)
            actualRelays shouldBe expectedRelays
        }
}

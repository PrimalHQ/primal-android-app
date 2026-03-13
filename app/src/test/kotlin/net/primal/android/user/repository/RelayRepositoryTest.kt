package net.primal.android.user.repository

import androidx.room.withTransaction
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.user.db.Relay as RelayPO
import net.primal.android.user.db.RelayDao
import net.primal.android.user.db.UsersDatabase
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.RelayKind
import net.primal.core.testing.CoroutinesTestRule
import net.primal.data.remote.api.users.UsersApi
import net.primal.data.remote.api.users.model.UsersRelaysResponse
import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEventKind
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("MaxLineLength")
@OptIn(ExperimentalCoroutinesApi::class)
class RelayRepositoryTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        mockkStatic("androidx.room.RoomDatabaseKt")
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
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

    private fun buildRelayDao(): RelayDao = mockk(relaxed = true)

    @Suppress("UNCHECKED_CAST")
    private fun buildUsersDatabase(relayDao: RelayDao = buildRelayDao()): UsersDatabase {
        val db = mockk<UsersDatabase>(relaxed = true) {
            every { relays() } returns relayDao
        }
        coEvery { db.withTransaction(any<suspend () -> Any?>()) } coAnswers {
            val block = args[1] as suspend () -> Any?
            block()
        }
        return db
    }

    private fun buildRepository(
        usersDatabase: UsersDatabase = buildUsersDatabase(),
        usersApi: UsersApi = mockk(relaxed = true),
        nostrPublisher: NostrPublisher = mockk(relaxed = true),
    ): RelayRepository {
        return RelayRepository(
            dispatchers = coroutinesTestRule.dispatcherProvider,
            usersDatabase = usersDatabase,
            usersApi = usersApi,
            nostrPublisher = nostrPublisher,
        )
    }

    @Test
    fun `bootstrapUserRelays calls getDefaultRelays and publishes relay list`() =
        runTest {
            val userId = "random"
            val expectedRelays = listOf("wss://relay.primal.net", "wss://relay.damus.io")
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val relayDao = buildRelayDao()
            val usersDatabase = buildUsersDatabase(relayDao)

            val repository = buildRepository(
                usersDatabase = usersDatabase,
                usersApi = mockk(relaxed = true) {
                    coEvery { getDefaultRelays() } returns expectedRelays
                },
                nostrPublisher = nostrPublisher,
            )

            repository.bootstrapUserRelays(userId = userId)

            coVerify {
                nostrPublisher.publishRelayList(
                    userId = userId,
                    relays = withArg { relays ->
                        relays.map { it.url }.sorted() shouldBe expectedRelays.sorted()
                    },
                )
            }
        }

    @Test
    fun `bootstrapUserRelays persists relays to database`() =
        runTest {
            val userId = "random"
            val expectedRelays = listOf("wss://relay.primal.net", "wss://relay.damus.io")
            val relayDao = buildRelayDao()
            val usersDatabase = buildUsersDatabase(relayDao)
            val upsertedRelaysSlot = slot<List<RelayPO>>()

            coEvery { relayDao.upsertAll(capture(upsertedRelaysSlot)) } returns Unit

            val repository = buildRepository(
                usersDatabase = usersDatabase,
                usersApi = mockk(relaxed = true) {
                    coEvery { getDefaultRelays() } returns expectedRelays
                },
            )

            repository.bootstrapUserRelays(userId = userId)

            coVerify { relayDao.deleteAll(userId = userId, kind = RelayKind.UserRelay) }
            upsertedRelaysSlot.captured.map { it.url }.sorted() shouldBe expectedRelays.sorted()
        }

    @Test
    fun `removeRelayAndPublishRelayList removes relay and publishes updated list`() =
        runTest {
            val userId = "random"
            val relays = listOf("wss://relay.primal.net", "wss://nostr1.current.fyi/")
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val relayDao = buildRelayDao()
            val usersDatabase = buildUsersDatabase(relayDao)

            val repository = buildRepository(
                usersDatabase = usersDatabase,
                usersApi = mockk(relaxed = true) {
                    coEvery { getUserRelays(listOf(userId)) } returns UsersRelaysResponse(
                        cachedRelayListEvents = listOf(buildPrimalUserRelaysListEvent(relays = relays)),
                    )
                },
                nostrPublisher = nostrPublisher,
            )

            repository.removeRelayAndPublishRelayList(userId = userId, url = relays.first())

            // After removing "wss://relay.primal.net", remaining relays from the API are used as-is
            val expectedRelayUrls = relays.drop(1)
            coVerify {
                nostrPublisher.publishRelayList(
                    userId = userId,
                    relays = withArg { publishedRelays ->
                        publishedRelays.map { it.url } shouldBe expectedRelayUrls
                    },
                )
            }
        }

    @Test
    fun `removeRelayAndPublishRelayList removes even if relay URL is not cleaned`() =
        runTest {
            val userId = "random"
            val relays = listOf("wss://nostr1.current.fyi", "wss://relay.primal.net")
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val relayDao = buildRelayDao()
            val usersDatabase = buildUsersDatabase(relayDao)

            val repository = buildRepository(
                usersDatabase = usersDatabase,
                usersApi = mockk(relaxed = true) {
                    coEvery { getUserRelays(listOf(userId)) } returns UsersRelaysResponse(
                        cachedRelayListEvents = listOf(buildPrimalUserRelaysListEvent(relays = relays)),
                    )
                },
                nostrPublisher = nostrPublisher,
            )

            // Removing with trailing slash should still match "wss://nostr1.current.fyi"
            // because removeIf compares cleaned URLs
            repository.removeRelayAndPublishRelayList(userId = userId, url = "wss://nostr1.current.fyi/")

            // Remaining relay URLs are from the API (not cleaned)
            val expectedRelayUrls = relays.drop(1)
            coVerify {
                nostrPublisher.publishRelayList(
                    userId = userId,
                    relays = withArg { publishedRelays ->
                        publishedRelays.map { it.url }.sorted() shouldBe expectedRelayUrls.sorted()
                    },
                )
            }
        }

    @Test
    fun `removeRelayAndPublishRelayList calls publish with remaining relay list`() =
        runTest {
            val userId = "random"
            val relays = listOf("wss://relay.primal.net", "wss://nostr1.current.fyi/")
            val nostrPublisher = mockk<NostrPublisher>(relaxed = true)
            val relayDao = buildRelayDao()
            val usersDatabase = buildUsersDatabase(relayDao)

            val repository = buildRepository(
                usersDatabase = usersDatabase,
                usersApi = mockk(relaxed = true) {
                    coEvery { getUserRelays(listOf(userId)) } returns UsersRelaysResponse(
                        cachedRelayListEvents = listOf(buildPrimalUserRelaysListEvent(relays = relays)),
                    )
                },
                nostrPublisher = nostrPublisher,
            )

            repository.removeRelayAndPublishRelayList(userId = userId, url = relays.first())
            // After removing first relay, remaining relays from the API are used as-is
            val expectedRelays = relays.drop(1).map { Relay(url = it, read = true, write = true) }

            coVerify {
                nostrPublisher.publishRelayList(
                    withArg { it shouldBe userId },
                    withArg { it shouldBe expectedRelays },
                )
            }
        }

    @Test
    fun `fetchAndUpdateUserRelays clears user relays if cached NIP-65 has empty tags`() =
        runTest {
            val userId = "random"
            val relayDao = buildRelayDao()
            val usersDatabase = buildUsersDatabase(relayDao)

            val repository = buildRepository(
                usersDatabase = usersDatabase,
                usersApi = mockk(relaxed = true) {
                    coEvery { getUserRelays(listOf(userId)) } returns UsersRelaysResponse(
                        cachedRelayListEvents = listOf(buildPrimalUserRelaysListEvent(relays = emptyList())),
                    )
                },
            )

            repository.fetchAndUpdateUserRelays(userId = userId)

            coVerify { relayDao.deleteAll(userId = userId, kind = RelayKind.UserRelay) }
            coVerify {
                relayDao.upsertAll(
                    relays = withArg { it shouldBe emptyList() },
                )
            }
        }

    @Test
    fun `fetchAndUpdateUserRelays ignores fetch if cached NIP-65 is missing`() =
        runTest {
            val userId = "random"
            val relayDao = buildRelayDao()
            val usersDatabase = buildUsersDatabase(relayDao)

            val repository = buildRepository(
                usersDatabase = usersDatabase,
                usersApi = mockk(relaxed = true) {
                    coEvery { getUserRelays(listOf(userId)) } returns
                        UsersRelaysResponse(cachedRelayListEvents = emptyList())
                },
            )

            repository.fetchAndUpdateUserRelays(userId = userId)

            coVerify(exactly = 0) { relayDao.deleteAll(userId = userId, kind = RelayKind.UserRelay) }
            coVerify(exactly = 0) { relayDao.upsertAll(any()) }
        }

    @Test
    fun `fetchAndUpdateUserRelays replaces relays when response has valid event`() =
        runTest {
            val userId = "random"
            val expectedRelayUrls = listOf("wss://relay.primal.net", "wss://relay.damus.io")
            val relayDao = buildRelayDao()
            val usersDatabase = buildUsersDatabase(relayDao)

            val repository = buildRepository(
                usersDatabase = usersDatabase,
                usersApi = mockk(relaxed = true) {
                    coEvery { getUserRelays(listOf(userId)) } returns UsersRelaysResponse(
                        cachedRelayListEvents = listOf(
                            buildPrimalUserRelaysListEvent(relays = expectedRelayUrls),
                        ),
                    )
                },
            )

            repository.fetchAndUpdateUserRelays(userId = userId)

            coVerify { relayDao.deleteAll(userId = userId, kind = RelayKind.UserRelay) }
            coVerify {
                relayDao.upsertAll(
                    relays = withArg { relays ->
                        relays.map { it.url }.sorted() shouldBe expectedRelayUrls.sorted()
                    },
                )
            }
        }
}

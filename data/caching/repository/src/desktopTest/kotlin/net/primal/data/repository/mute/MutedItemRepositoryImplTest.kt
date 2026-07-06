package net.primal.data.repository.mute

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.db.CachingDatabase
import net.primal.data.remote.api.settings.SettingsApi
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asFollowedMuteListPubkeyTag
import net.primal.domain.nostr.asIdentifierTag
import net.primal.domain.nostr.followedMuteListIdentifierTag
import net.primal.domain.nostr.publisher.NostrPublishException
import net.primal.domain.publisher.PrimalPublishResult
import net.primal.domain.publisher.PrimalPublisher

class MutedItemRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private val userId = "test_user_id"
    private val authorA = "author_a_pubkey"
    private val authorB = "author_b_pubkey"

    private val dummyNostrEvent = NostrEvent(
        id = "signed_event_id",
        pubKey = userId,
        createdAt = 0,
        kind = NostrEventKind.CategorizedPeopleList.value,
        tags = emptyList(),
        content = "",
        sig = "signature",
    )
    private val dummyPublishResult = PrimalPublishResult(nostrEvent = dummyNostrEvent)

    private val dispatcherProvider: DispatcherProvider = mockk {
        every { io() } returns testDispatcher
        every { main() } returns testDispatcher
    }

    private val mockDatabase: CachingDatabase = mockk(relaxed = true)

    private fun buildRepository(settingsApi: SettingsApi, primalPublisher: PrimalPublisher) =
        MutedItemRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = mockDatabase,
            settingsApi = settingsApi,
            primalPublisher = primalPublisher,
        )

    private fun followedMuteListEvent(content: String, followedAuthorIds: List<String>) =
        dummyNostrEvent.copy(
            content = content,
            tags = listOf(followedMuteListIdentifierTag()) +
                followedAuthorIds.map { it.asFollowedMuteListPubkeyTag() },
        )

    @Test
    fun `followMuteList publishes a new kind 30000 event when none exists`() =
        runTest(testDispatcher) {
            val settingsApi = mockk<SettingsApi> {
                coEvery { getFollowedMuteListEvent(userId) } returns null
            }
            val publishedSlot = slot<NostrUnsignedEvent>()
            val publisher = mockk<PrimalPublisher> {
                coEvery { signPublishImportNostrEvent(capture(publishedSlot), any()) } returns dummyPublishResult
            }

            buildRepository(settingsApi, publisher)
                .followMuteList(userId = userId, muteListOwnerId = authorA)

            val published = publishedSlot.captured
            published.kind shouldBe NostrEventKind.CategorizedPeopleList.value
            published.pubKey shouldBe userId
            published.content shouldBe ""
            published.tags shouldBe listOf(
                followedMuteListIdentifierTag(),
                authorA.asFollowedMuteListPubkeyTag(),
            )
        }

    @Test
    fun `followMuteList appends to the existing event and preserves its content and d-tag`() =
        runTest(testDispatcher) {
            val existing = followedMuteListEvent(content = "existing-content", followedAuthorIds = listOf(authorA))
            val settingsApi = mockk<SettingsApi> {
                coEvery { getFollowedMuteListEvent(userId) } returns existing
            }
            val publishedSlot = slot<NostrUnsignedEvent>()
            val publisher = mockk<PrimalPublisher> {
                coEvery { signPublishImportNostrEvent(capture(publishedSlot), any()) } returns dummyPublishResult
            }

            buildRepository(settingsApi, publisher)
                .followMuteList(userId = userId, muteListOwnerId = authorB)

            val published = publishedSlot.captured
            published.content shouldBe "existing-content"
            published.tags shouldBe listOf(
                followedMuteListIdentifierTag(),
                authorA.asFollowedMuteListPubkeyTag(),
                authorB.asFollowedMuteListPubkeyTag(),
            )
        }

    @Test
    fun `followMuteList ignores a non-mutelists event and creates a fresh list`() =
        runTest(testDispatcher) {
            val otherList = dummyNostrEvent.copy(
                content = "other",
                tags = listOf("other-list".asIdentifierTag(), authorA.asFollowedMuteListPubkeyTag()),
            )
            val settingsApi = mockk<SettingsApi> {
                coEvery { getFollowedMuteListEvent(userId) } returns otherList
            }
            val publishedSlot = slot<NostrUnsignedEvent>()
            val publisher = mockk<PrimalPublisher> {
                coEvery { signPublishImportNostrEvent(capture(publishedSlot), any()) } returns dummyPublishResult
            }

            buildRepository(settingsApi, publisher)
                .followMuteList(userId = userId, muteListOwnerId = authorB)

            val published = publishedSlot.captured
            published.content shouldBe ""
            published.tags shouldBe listOf(
                followedMuteListIdentifierTag(),
                authorB.asFollowedMuteListPubkeyTag(),
            )
        }

    @Test
    fun `followMuteList is a no-op when the author is already followed`() =
        runTest(testDispatcher) {
            val existing = followedMuteListEvent(content = "", followedAuthorIds = listOf(authorA))
            val settingsApi = mockk<SettingsApi> {
                coEvery { getFollowedMuteListEvent(userId) } returns existing
            }
            val publisher = mockk<PrimalPublisher>()

            buildRepository(settingsApi, publisher)
                .followMuteList(userId = userId, muteListOwnerId = authorA)

            coVerify(exactly = 0) { publisher.signPublishImportNostrEvent(any(), any()) }
        }

    @Test
    fun `followMuteList returns a failure Result when publishing fails`() =
        runTest(testDispatcher) {
            val settingsApi = mockk<SettingsApi> {
                coEvery { getFollowedMuteListEvent(userId) } returns null
            }
            val publisher = mockk<PrimalPublisher> {
                coEvery { signPublishImportNostrEvent(any(), any()) } throws NostrPublishException(cause = null)
            }

            val result = buildRepository(settingsApi, publisher)
                .followMuteList(userId = userId, muteListOwnerId = authorA)

            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NostrPublishException>()
        }
}

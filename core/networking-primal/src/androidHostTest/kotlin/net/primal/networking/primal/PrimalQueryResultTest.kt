package net.primal.networking.primal

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid
import net.primal.core.networking.primal.PrimalQueryResult
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.isNotPrimalEventKind
import net.primal.domain.nostr.isNotUnknown
import net.primal.domain.nostr.isPrimalEventKind
import org.junit.Test

class PrimalQueryResultTest {

    private val nostrEvents = List(50) {
        NostrEventKind.entries
            .random()
            .takeIf { it.isNotPrimalEventKind() && !it.isNotUnknown() }
            .let {
                buildNostrEvent(kind = it ?: NostrEventKind.Unknown)
            }
    }

    private val primalEvents = List(20) {
        NostrEventKind.entries
            .random()
            .takeIf { it.isPrimalEventKind() }
            .let {
                buildPrimalEvent(kind = it ?: NostrEventKind.Unknown)
            }
    }

    private fun buildNostrEvent(kind: NostrEventKind) =
        NostrEvent(
            id = "",
            content = "",
            createdAt = 0,
            pubKey = "",
            kind = kind.value,
            sig = "",
        )

    private fun buildPrimalEvent(kind: NostrEventKind) =
        PrimalEvent(
            kind = kind.value,
            content = "",
        )

    private fun buildQueryResult(
        nostrEvents: List<NostrEvent> = emptyList(),
        primalEvents: List<PrimalEvent> = emptyList(),
    ) = PrimalQueryResult(
        terminationMessage = NostrIncomingMessage.EoseMessage(
            subscriptionId = Uuid.random().toString(),
        ),
        nostrEvents = nostrEvents,
        primalEvents = primalEvents,
    )

    @Test
    fun `findNostrEvent finds nostr event for given kind`() {
        val expectedKind = NostrEventKind.Metadata
        val expected = buildNostrEvent(kind = expectedKind)
        val queryResult = buildQueryResult(
            nostrEvents = nostrEvents.toMutableList().apply {
                removeAll { it.kind == expectedKind.value }
                add(expected)
            }.toList(),
        )

        val actual = queryResult.findNostrEvent(kind = expectedKind)
        actual shouldBe expected
    }

    @Test
    fun `findNostrEvent returns null for missing event`() {
        val queryResult = buildQueryResult()

        val actual = queryResult.findNostrEvent(kind = NostrEventKind.PrimalCdnResource)
        actual.shouldBeNull()
    }

    @Test
    fun `findPrimalEvent finds nostr event for given kind`() {
        val expectedKind = NostrEventKind.PrimalAppState
        val expected = buildPrimalEvent(kind = expectedKind)
        val queryResult = buildQueryResult(
            primalEvents = primalEvents.toMutableList().apply {
                removeAll { it.kind == expectedKind.value }
                add(expected)
            }.toList(),
        )

        val actual = queryResult.findPrimalEvent(kind = expectedKind)
        actual shouldBe expected
    }

    @Test
    fun `findPrimalEvent returns null for missing event`() {
        val queryResult = buildQueryResult()

        val actual = queryResult.findPrimalEvent(kind = NostrEventKind.FollowList)
        actual.shouldBeNull()
    }

    @Test
    fun `filterNostrEvents filters nostr events for given kind`() {
        val expectedKind = NostrEventKind.FollowList
        val expected = buildNostrEvent(kind = expectedKind)
        val queryResult = buildQueryResult(
            nostrEvents = nostrEvents.toMutableList().apply {
                add(expected)
            }.toList(),
        )

        val actual = queryResult.filterNostrEvents(kind = expectedKind)
        actual.shouldNotBeEmpty()
        actual shouldContain expected
    }

    @Test
    fun `filterNostrEvents returns empty list if no events found`() {
        val queryResult = buildQueryResult()

        val actual = queryResult.filterNostrEvents(kind = NostrEventKind.PrimalCdnResource)
        actual.shouldBeEmpty()
    }

    @Test
    fun `filterPrimalEvents filters nostr events for given kind`() {
        val expectedKind = NostrEventKind.PrimalUserProfileStats
        val expected = buildPrimalEvent(kind = expectedKind)
        val queryResult = buildQueryResult(
            primalEvents = primalEvents.toMutableList().apply {
                add(expected)
            }.toList(),
        )

        val actual = queryResult.filterPrimalEvents(kind = expectedKind)
        actual.shouldNotBeEmpty()
        actual shouldContain expected
    }

    @Test
    fun `filterPrimalEvents returns empty list if no events found`() {
        val queryResult = buildQueryResult()

        val actual = queryResult.filterPrimalEvents(kind = NostrEventKind.EncryptedDirectMessages)
        actual.shouldBeEmpty()
    }
}

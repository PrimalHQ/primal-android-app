package net.primal.android.networking.primal

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.util.*
import net.primal.android.networking.sockets.NostrIncomingMessage
import net.primal.android.nostr.ext.isNotPrimalEventKind
import net.primal.android.nostr.ext.isNotUnknown
import net.primal.android.nostr.ext.isPrimalEventKind
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.PrimalEvent
import org.junit.Test

class PrimalQueryResultTest {

    private val nostrEvents = List(50) {
        NostrEventKind.values()
            .random()
            .takeIf { it.isNotPrimalEventKind() && !it.isNotUnknown()}
            .let {
                buildNostrEvent(kind = it ?: NostrEventKind.Unknown)
            }
    }

    private val primalEvents = List(20) {
        NostrEventKind.values()
            .random()
            .takeIf { it.isPrimalEventKind() }
            .let {
                buildPrimalEvent(kind = it ?: NostrEventKind.Unknown)
            }
    }

    private fun buildNostrEvent(kind: NostrEventKind) = NostrEvent(
        id = "", content = "", createdAt = 0, pubKey = "", kind = kind.value, sig = ""
    )

    private fun buildPrimalEvent(kind: NostrEventKind) = PrimalEvent(
        kind = kind.value, content = "",
    )

    private fun buildQueryResult(
        nostrEvents: List<NostrEvent> = emptyList(),
        primalEvents: List<PrimalEvent> = emptyList(),
    ) = PrimalQueryResult(
        terminationMessage = NostrIncomingMessage.EoseMessage(
            subscriptionId = UUID.randomUUID(),
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
            }.toList()
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
            }.toList()
        )

        val actual = queryResult.findPrimalEvent(kind = expectedKind)
        actual shouldBe expected
    }

    @Test
    fun `findPrimalEvent returns null for missing event`() {
        val queryResult = buildQueryResult()

        val actual = queryResult.findPrimalEvent(kind = NostrEventKind.Contacts)
        actual.shouldBeNull()
    }

    @Test
    fun `filterNostrEvents filters nostr events for given kind`() {
        val expectedKind = NostrEventKind.Contacts
        val expected = buildNostrEvent(kind = expectedKind)
        val queryResult = buildQueryResult(
            nostrEvents = nostrEvents.toMutableList().apply {
                add(expected)
            }.toList()
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
            }.toList()
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

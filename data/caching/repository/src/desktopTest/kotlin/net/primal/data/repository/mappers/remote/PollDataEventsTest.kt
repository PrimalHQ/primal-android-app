package net.primal.data.repository.mappers.remote

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

class PollDataEventsTest {

    private fun tag(vararg values: String) = JsonArray(values.map(::JsonPrimitive))

    private fun zapPollEvent(tags: List<JsonArray>) =
        NostrEvent(
            id = "poll-event-id",
            pubKey = POLL_AUTHOR,
            createdAt = 1_700_000_000L,
            kind = NostrEventKind.ZapPoll.value,
            tags = tags,
            content = "Zap poll",
            sig = "sig",
        )

    @Test
    fun mapNotNullAsPollDataPO_resolvesZapRecipientToPollAuthor_forTopLevelZapPoll() {
        val event = zapPollEvent(
            tags = listOf(
                tag("poll_option", "0", "Option A"),
                tag("poll_option", "1", "Option B"),
                tag("closed_at", "1700003600"),
            ),
        )

        val poll = listOf(event).mapNotNullAsPollDataPO().single()

        assertEquals(POLL_AUTHOR, poll.zapRecipientId)
    }

    @Test
    fun mapNotNullAsPollDataPO_resolvesZapRecipientToPollAuthor_forReplyComposedZapPoll() {
        val event = zapPollEvent(
            tags = listOf(
                tag("e", "root-note-id", "", "root"),
                tag("p", REPLIED_TO_AUTHOR, "wss://relay.example.com"),
                tag("poll_option", "0", "Option A"),
                tag("poll_option", "1", "Option B"),
                tag("closed_at", "1700003600"),
            ),
        )

        val poll = listOf(event).mapNotNullAsPollDataPO().single()

        assertEquals(POLL_AUTHOR, poll.zapRecipientId)
    }

    @Test
    fun mapNotNullAsPollDataPO_resolvesZapRecipientToPollAuthor_whenAuthorPTagFollowsReferenceTags() {
        val event = zapPollEvent(
            tags = listOf(
                tag("e", "root-note-id", "", "root"),
                tag("p", REPLIED_TO_AUTHOR, "wss://relay.example.com"),
                tag("poll_option", "0", "Option A"),
                tag("p", POLL_AUTHOR, "wss://relay.example.com"),
                tag("closed_at", "1700003600"),
            ),
        )

        val poll = listOf(event).mapNotNullAsPollDataPO().single()

        assertEquals(POLL_AUTHOR, poll.zapRecipientId)
    }

    companion object {
        private const val POLL_AUTHOR = "poll-author-pubkey"
        private const val REPLIED_TO_AUTHOR = "replied-to-author-pubkey"
    }
}

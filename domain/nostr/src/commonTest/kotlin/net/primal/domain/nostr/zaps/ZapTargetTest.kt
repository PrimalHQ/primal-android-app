package net.primal.domain.nostr.zaps

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.serialization.json.jsonPrimitive

class ZapTargetTest {

    private val recipientUserId = "recipient_user_id"
    private val recipientLnUrl = "lnurl_decoded"

    @Test
    fun `PollEvent toTags includes pubkey and event id and poll option tags`() {
        val eventId = "poll_event_id"
        val optionId = "0"
        val target = ZapTarget.PollEvent(
            eventId = eventId,
            optionId = optionId,
            recipientUserId = recipientUserId,
            recipientLnUrlDecoded = recipientLnUrl,
        )

        val tags = target.toTags()

        tags.size shouldBe 3
        tags[0][0].jsonPrimitive.content shouldBe "p"
        tags[0][1].jsonPrimitive.content shouldBe recipientUserId
        tags[1][0].jsonPrimitive.content shouldBe "e"
        tags[1][1].jsonPrimitive.content shouldBe eventId
        tags[2][0].jsonPrimitive.content shouldBe "poll_option"
        tags[2][1].jsonPrimitive.content shouldBe optionId
    }

    @Test
    fun `Event toTags does not include poll option tag`() {
        val target = ZapTarget.Event(
            eventId = "event_id",
            recipientUserId = recipientUserId,
            recipientLnUrlDecoded = recipientLnUrl,
        )

        val tags = target.toTags()

        tags.size shouldBe 2
        tags[0][0].jsonPrimitive.content shouldBe "p"
        tags[1][0].jsonPrimitive.content shouldBe "e"
    }

    @Test
    fun `PollEvent toTags produces same event and pubkey tags as Event toTags`() {
        val eventId = "shared_event_id"
        val pollTarget = ZapTarget.PollEvent(
            eventId = eventId,
            optionId = "1",
            recipientUserId = recipientUserId,
            recipientLnUrlDecoded = recipientLnUrl,
        )
        val eventTarget = ZapTarget.Event(
            eventId = eventId,
            recipientUserId = recipientUserId,
            recipientLnUrlDecoded = recipientLnUrl,
        )

        val pollTags = pollTarget.toTags()
        val eventTags = eventTarget.toTags()

        pollTags[0] shouldBe eventTags[0]
        pollTags[1] shouldBe eventTags[1]
    }
}

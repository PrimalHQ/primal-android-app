package net.primal.android.networking.sockets

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Test
import java.util.UUID

class NostrOutgoingMessageBuilderTest {

    @Test
    fun `buildNostrREQMessage returns correct text message`() {
        val subscriptionId = UUID.randomUUID()
        val filter = buildJsonObject {
            put("random", "random")
        }

        val actual = filter.buildNostrREQMessage(subscriptionId)
        actual shouldBe """["${NostrVerb.Outgoing.REQ}","$subscriptionId",$filter]"""
    }

    @Test
    fun `buildNostrEVENTMessage returns correct text message`() {
        val nostrEvent = buildJsonObject {
            put("random", "random")
        }

        val actual = nostrEvent.buildNostrEVENTMessage()
        actual shouldBe """["${NostrVerb.Outgoing.EVENT}",$nostrEvent]"""
    }

    @Test
    fun `buildNostrAUTHMessage returns correct text message`() {
        val nostrEvent = buildJsonObject {
            put("random", "random")
        }

        val actual = nostrEvent.buildNostrAUTHMessage()
        actual shouldBe """["${NostrVerb.Outgoing.AUTH}",$nostrEvent]"""
    }

    @Test
    fun `buildNostrCOUNTMessage returns correct text message`() {
        val subscriptionId = UUID.randomUUID()
        val filter = buildJsonObject {
            put("random", "random")
        }

        val actual = filter.buildNostrCOUNTMessage(subscriptionId)
        actual shouldBe """["${NostrVerb.Outgoing.COUNT}","$subscriptionId",$filter]"""
    }

    @Test
    fun `buildNostrCLOSEMessage returns correct text message`() {
        val subscriptionId = UUID.randomUUID()
        val actual = subscriptionId.buildNostrCLOSEMessage()
        actual shouldBe """["${NostrVerb.Outgoing.CLOSE}","$subscriptionId"]"""
    }

}

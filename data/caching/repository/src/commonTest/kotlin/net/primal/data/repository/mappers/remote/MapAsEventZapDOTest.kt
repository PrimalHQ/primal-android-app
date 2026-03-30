package net.primal.data.repository.mappers.remote

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.events.ZapKind
import net.primal.domain.nostr.NostrEvent

@Suppress("MaxLineLength")
class MapAsEventZapDOTest {

    private val senderId = "sender_pubkey"
    private val receiverId = "receiver_pubkey"
    private val eventId = "zapped_event_id"

    private val bolt11Invoice =
        "lnbc123450n1pj7welppp53umfyxp6jn9uvkt463hydtq2zpfvz78hxhpfv9wqx6v4uwdw2rnqdzqg3hkuct5v56zu3n4dcsxgmmwv96x" +
            "jmmwyp6x7gzqgpc8ymmrv4h8ghmrwfuhqar0cqzpgxqrrsssp5tntqjpngx6l8y9va9tzd7fmtemtyp5vvsqphw8f8yqjjrr26x5qs9" +
            "qyyssqyyv7tqp5kpsmv6s5825kcq8fxsn4ag2h5uj2j6lnsnclyyq6844khayzqrl7yue46nwlukfr4uftqcwzxzh8krqg9rqsg9tg6x" +
            "ggszcp0gyjcd"

    private fun tag(vararg values: String): JsonArray = buildJsonArray { values.forEach { add(JsonPrimitive(it)) } }

    private fun buildZapRequest(tags: List<JsonArray> = emptyList(), content: String = ""): NostrEvent {
        return NostrEvent(
            id = "zap_request_id",
            pubKey = senderId,
            createdAt = 1000L,
            kind = 9734,
            tags = tags,
            content = content,
            sig = "zap_request_sig",
        )
    }

    private fun buildZapReceipt(zapRequest: NostrEvent): NostrEvent {
        val zapRequestJson = zapRequest.encodeToJsonString()
        return NostrEvent(
            id = "zap_receipt_id",
            pubKey = "relay_pubkey",
            createdAt = 2000L,
            kind = 9735,
            tags = listOf(
                tag("p", receiverId),
                tag("e", eventId),
                tag("bolt11", bolt11Invoice),
                tag("description", zapRequestJson),
            ),
            content = "",
            sig = "zap_receipt_sig",
        )
    }

    @Test
    fun `mapAsEventZapDO assigns GENERIC zapKind for regular zaps`() {
        val zapRequest = buildZapRequest(
            tags = listOf(tag("p", receiverId), tag("e", eventId)),
        )
        val zapReceipt = buildZapReceipt(zapRequest)

        val result = listOf(zapReceipt).mapAsEventZapDO(profilesMap = emptyMap())

        result shouldHaveSize 1
        result.first().zapKind shouldBe ZapKind.GENERIC
    }

    @Test
    fun `mapAsEventZapDO assigns VOTE zapKind for poll zaps`() {
        val zapRequest = buildZapRequest(
            tags = listOf(tag("p", receiverId), tag("e", eventId), tag("poll_option", "0")),
        )
        val zapReceipt = buildZapReceipt(zapRequest)

        val result = listOf(zapReceipt).mapAsEventZapDO(profilesMap = emptyMap())

        result shouldHaveSize 1
        result.first().zapKind shouldBe ZapKind.VOTE
    }

    @Test
    fun `mapAsEventZapDO includes both GENERIC and VOTE zaps in output`() {
        val genericRequest = buildZapRequest(
            tags = listOf(tag("p", receiverId), tag("e", eventId)),
        )
        val pollRequest = buildZapRequest(
            tags = listOf(tag("p", receiverId), tag("e", eventId), tag("poll_option", "1")),
        )

        val result = listOf(
            buildZapReceipt(genericRequest),
            buildZapReceipt(pollRequest),
        ).mapAsEventZapDO(profilesMap = emptyMap())

        result shouldHaveSize 2
        result.count { it.zapKind == ZapKind.GENERIC } shouldBe 1
        result.count { it.zapKind == ZapKind.VOTE } shouldBe 1
    }
}

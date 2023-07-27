package net.primal.android.serialization

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import net.primal.android.nostr.model.NostrEvent
import org.junit.Test

class NostrEventJsonEncodeTest {

    private fun buildNostrEvent(
        tags: List<JsonArray>,
    ): NostrEvent = NostrEvent(
        id = "id",
        pubKey = "pubkey",
        createdAt = 100L,
        kind = 100,
        tags = tags,
        content = "",
        sig = "signature",
    )

    @Test
    fun `toJsonObject serializes properly nostr event`() {
        val tags = listOf(
            buildJsonArray {
                add("e")
                add("eventId")
            }
        )
        val nostrEvent = buildNostrEvent(tags = tags)
        val actual = nostrEvent.toJsonObject()
        actual shouldContainFieldsFrom nostrEvent
    }

    @Test
    fun `toJsonObject serializes tags as empty array if null`() {
        val nostrEvent = buildNostrEvent(tags = emptyList())
        val actual = nostrEvent.toJsonObject()
        actual shouldContainFieldsFrom nostrEvent
    }

    private infix fun JsonObject.shouldContainFieldsFrom(expected: NostrEvent?) {
        expected.shouldNotBeNull()
        this["id"].shouldNotBeNull()
        this["id"]?.jsonPrimitive?.content shouldBe expected.id

        this["pubkey"].shouldNotBeNull()
        this["pubkey"]?.jsonPrimitive?.content shouldBe expected.pubKey

        this["created_at"].shouldNotBeNull()
        this["created_at"]?.jsonPrimitive?.long shouldBe expected.createdAt

        this["kind"].shouldNotBeNull()
        this["kind"]?.jsonPrimitive?.int shouldBe expected.kind

        this["tags"].shouldNotBeNull()
        this["tags"]?.jsonArray shouldBe expected.tags

        this["content"].shouldNotBeNull()
        this["content"]?.jsonPrimitive?.content shouldBe expected.content

        this["sig"].shouldNotBeNull()
        this["sig"]?.jsonPrimitive?.content shouldBe expected.sig
    }

}

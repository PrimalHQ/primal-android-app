package net.primal.android.core.serialization.json

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
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

class NostrJsonTest {

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
    fun `decodeFromStringOrNull returns decoded object`() {
        val actual = NostrJson.decodeFromStringOrNull<NostrEvent>(
            """
                {
                	"id": "1c6a86ab9e68e3a32e6f1c8503890dd8fd62a124d081c75114faf3edcbe50384",
                	"pubkey": "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a",
                	"created_at": 1690477830,
                	"kind": 1,
                	"content": "Any Android users out there? \n\nWould you like to try the new Primal Android build? ",
                	"sig": "d42332eb0e3d6ca268ab89cecc6f61cb545916749c492d35d48baf2b5959cfd930fd6c04645591aeb2c75b08d8a684d6f99ee6d738a1f2e10867ded1c4bd2f62"
                }
            """.trimIndent()
        )
        actual.shouldNotBeNull()
        actual.shouldBeTypeOf<NostrEvent>()
    }

    @Test
    fun `decodeFromStringOrNull returns null for null input`() {
        val actual = NostrJson.decodeFromStringOrNull<NostrEvent>(null)
        actual.shouldBeNull()
    }

    @Test
    fun `decodeFromStringOrNull returns null for invalid input`() {
        val actual = NostrJson.decodeFromStringOrNull<NostrEvent>("invalid")
        actual.shouldBeNull()
    }

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

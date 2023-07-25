package net.primal.android.nostr.notary

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test

class TagExtTest {

    @Test
    fun `asEventIdTag returns proper JsonArray tag`() {
        val eventId = "eventId"
        val actual = eventId.asEventIdTag()
        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 2
        actual[0].jsonPrimitive.content shouldBe "e"
        actual[1].jsonPrimitive.content shouldBe eventId
    }

    @Test
    fun `asPubkeyTag returns proper JsonArray tag`() {
        val pubkey = "myPubkey"
        val actual = pubkey.asPubkeyTag()
        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 2
        actual[0].jsonPrimitive.content shouldBe "p"
        actual[1].jsonPrimitive.content shouldBe pubkey
    }

    @Test
    fun `asIdentifierTag returns proper JsonArray tag`() {
        val identifier = "Primal App"
        val actual = identifier.asIdentifierTag()
        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 2
        actual[0].jsonPrimitive.content shouldBe "d"
        actual[1].jsonPrimitive.content shouldBe identifier
    }
}

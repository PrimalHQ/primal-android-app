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
        val expectedRecommendedRelay = ""
        val expectedMarker = "root"
        val actual = eventId.asEventIdTag(
            recommendedRelay = expectedRecommendedRelay,
            marker = expectedMarker,
        )
        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 4
        actual[0].jsonPrimitive.content shouldBe "e"
        actual[1].jsonPrimitive.content shouldBe eventId
        actual[2].jsonPrimitive.content shouldBe expectedRecommendedRelay
        actual[3].jsonPrimitive.content shouldBe expectedMarker
    }

    @Test
    fun `asPubkeyTag returns proper JsonArray tag`() {
        val pubkey = "myPubkey"
        val expectedRecommendedRelay = ""
        val expectedMarker = "mention"
        val actual = pubkey.asPubkeyTag(
            recommendedRelay = expectedRecommendedRelay,
            marker = expectedMarker,
        )
        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 4
        actual[0].jsonPrimitive.content shouldBe "p"
        actual[1].jsonPrimitive.content shouldBe pubkey
        actual[2].jsonPrimitive.content shouldBe expectedRecommendedRelay
        actual[3].jsonPrimitive.content shouldBe expectedMarker
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

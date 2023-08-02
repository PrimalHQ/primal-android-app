package net.primal.android.nostr.ext

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test

class TagsTest {

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

    @Test
    fun `parseEventTags returns tags for nostrnevent`() {
        val content = "nostr:nevent1qqs0ejzkvqdlqaej8k7edamkvzcnjv77u6npmp2qhdpvswyjvcplafqpp4mhxue69uhkummn9ekx7mqzyrhxagf6h8l9cjngatumrg60uq22v66qz979pm32v985ek54ndh8gqcyqqqqqqgpldx8x"
        val actual = content.parseEventTags().firstOrNull()
        actual.shouldNotBeNull()
        actual[0].jsonPrimitive.content shouldBe "e"
        actual[1].jsonPrimitive.content shouldBe "fcc856601bf077323dbd96f77660b13933dee6a61d8540bb42c838926603fea4"
        actual[2].jsonPrimitive.content shouldBe "wss://nos.lol"
    }

    @Test
    fun `parseEventTags returns tags for nostrnote`() {
        val content = "nostr:note1zwwmjqpaymqhjlq0tjezcyhpcx0hyh4m3u5p54tm7mmfxaqg03pq3w3d6g"
        val actual = content.parseEventTags().firstOrNull()
        actual.shouldNotBeNull()
        actual[0].jsonPrimitive.content shouldBe "e"
        actual[1].jsonPrimitive.content shouldBe "139db9003d26c1797c0f5cb22c12e1c19f725ebb8f281a557bf6f69374087c42"
    }

    @Test
    fun `parseEventTags returns tags for note`() {
        val content = "note1zwwmjqpaymqhjlq0tjezcyhpcx0hyh4m3u5p54tm7mmfxaqg03pq3w3d6g"
        val actual = content.parseEventTags().firstOrNull()
        actual.shouldNotBeNull()
        actual[0].jsonPrimitive.content shouldBe "e"
        actual[1].jsonPrimitive.content shouldBe "139db9003d26c1797c0f5cb22c12e1c19f725ebb8f281a557bf6f69374087c42"
    }

    @Test
    fun `parseHashtagTags returns tags for hashtags`() {
        val content = "This hashtags in brackets (#Nostr, #Bitcoin, #Primal) should be fine!"
        val expectedHashtags = listOf("Nostr", "Bitcoin", "Primal")
        val actual = content.parseHashtagTags()
        actual.size shouldBe 3
        actual.forEachIndexed { index, tag ->
            tag[0].jsonPrimitive.content shouldBe "t"
            tag[1].jsonPrimitive.content shouldBe expectedHashtags[index]
        }
    }

}

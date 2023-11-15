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
        val actual = pubkey.asPubkeyTag(recommendedRelay = expectedRecommendedRelay)
        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 3
        actual[0].jsonPrimitive.content shouldBe "p"
        actual[1].jsonPrimitive.content shouldBe pubkey
        actual[2].jsonPrimitive.content shouldBe expectedRecommendedRelay
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
        val content = "nostr:nevent1qqs0ejzkvqdlqaej8k7edamkvzcnjv77u6npmp2qhdpv" +
                "swyjvcplafqpp4mhxue69uhkummn9ekx7mqzyrhxagf6h8l9cjngatumrg60uq2" +
                "2v66qz979pm32v985ek54ndh8gqcyqqqqqqgpldx8x"
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

    @Test
    fun `parsePubkeyTags returns tags for nostrnprofile`() {
        val content = "nostr:nprofile1qqsrhuxx8l9ex335q7he0f09aej04zpazpl" +
                "0ne2cgukyawd24mayt8gpp4mhxue69uhhytnc9e3k7mgpz4mhxue69uh" +
                "kg6nzv9ejuumpv34kytnrdaksjlyr9p"
        val actual = content.parsePubkeyTags().firstOrNull()
        actual.shouldNotBeNull()
        actual[0].jsonPrimitive.content shouldBe "p"
        actual[1].jsonPrimitive.content shouldBe "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d"
        actual[2].jsonPrimitive.content shouldBe "wss://r.x.com"
    }

    @Test
    fun `parsePubkeyTags returns tags for nprofile`() {
        val content = "nprofile1qqsrhuxx8l9ex335q7he0f09aej04zpazpl0ne2cgukyawd24mayt" +
                "8gpp4mhxue69uhhytnc9e3k7mgpz4mhxue69uhkg6nzv9ejuumpv34kytnrdaksjlyr9p"
        val actual = content.parsePubkeyTags().firstOrNull()
        actual.shouldNotBeNull()
        actual[0].jsonPrimitive.content shouldBe "p"
        actual[1].jsonPrimitive.content shouldBe "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d"
        actual[2].jsonPrimitive.content shouldBe "wss://r.x.com"
    }

    @Test
    fun `parsePubkeyTags returns tags for nostrnpub`() {
        val content = "nostr:npub10elfcs4fr0l0r8af98jlmgdh9c8tcxjvz9qkw038js35mp4dma8qzvjptg"
        val actual = content.parsePubkeyTags().firstOrNull()
        actual.shouldNotBeNull()
        actual[0].jsonPrimitive.content shouldBe "p"
        actual[1].jsonPrimitive.content shouldBe "7e7e9c42a91bfef19fa929e5fda1b72e0ebc1a4c1141673e2794234d86addf4e"
    }

    @Test
    fun `parsePubkeyTags returns tags for npub`() {
        val content = "npub10elfcs4fr0l0r8af98jlmgdh9c8tcxjvz9qkw038js35mp4dma8qzvjptg"
        val actual = content.parsePubkeyTags().firstOrNull()
        actual.shouldNotBeNull()
        actual[0].jsonPrimitive.content shouldBe "p"
        actual[1].jsonPrimitive.content shouldBe "7e7e9c42a91bfef19fa929e5fda1b72e0ebc1a4c1141673e2794234d86addf4e"
    }

}

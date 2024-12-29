package net.primal.android.nostr.ext

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.utils.Naddr
import net.primal.android.nostr.utils.Nevent
import net.primal.android.nostr.utils.Nip19TLV.toNaddrString
import net.primal.android.nostr.utils.asATagValue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagsTest {

    @Test
    fun `asContextTag returns proper JsonArray tag`() {
        val eventId = "eventId"

        val actual = eventId.asContextTag()

        actual shouldBe instanceOf(JsonArray::class)
        actual[0].jsonPrimitive.content shouldBe "context"
        actual[1].jsonPrimitive.content shouldBe eventId
    }

    @Test
    fun `asAltTag returns proper JsonArray tag`() {
        val eventId = "eventId"

        val actual = eventId.asAltTag()

        actual shouldBe instanceOf(JsonArray::class)
        actual[0].jsonPrimitive.content shouldBe "alt"
        actual[1].jsonPrimitive.content shouldBe eventId
    }

    @Test
    fun `asKindTag returns proper JsonArray tag`() {
        val nostrKind = NostrEventKind.LongFormContent

        val actual = nostrKind.asKindTag()

        actual shouldBe instanceOf(JsonArray::class)
        actual[0].jsonPrimitive.content shouldBe "k"
        actual[1].jsonPrimitive.content shouldBe nostrKind.value.toString()
    }

    @Test
    fun `asEventIdTag returns proper JsonArray tag if optional args null`() {
        val eventId = "eventId"
        val expectedRecommendedRelay = ""
        val expectedMarker = ""
        val expectedAuthorId = ""

        val actual = eventId.asEventIdTag(
            relayHint = null,
            marker = null,
            authorPubkey = null,
        )
        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 5
        actual[0].jsonPrimitive.content shouldBe "e"
        actual[1].jsonPrimitive.content shouldBe eventId
        actual[2].jsonPrimitive.content shouldBe expectedRecommendedRelay
        actual[3].jsonPrimitive.content shouldBe expectedMarker
        actual[4].jsonPrimitive.content shouldBe expectedAuthorId
    }

    @Test
    fun `asEventIdTag returns proper JsonArray tag`() {
        val eventId = "eventId"
        val expectedRecommendedRelay = ""
        val expectedMarker = "root"
        val expectedAuthorId = "authorId"

        val actual = eventId.asEventIdTag(
            relayHint = expectedRecommendedRelay,
            marker = expectedMarker,
            authorPubkey = expectedAuthorId,
        )
        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 5
        actual[0].jsonPrimitive.content shouldBe "e"
        actual[1].jsonPrimitive.content shouldBe eventId
        actual[2].jsonPrimitive.content shouldBe expectedRecommendedRelay
        actual[3].jsonPrimitive.content shouldBe expectedMarker
        actual[4].jsonPrimitive.content shouldBe expectedAuthorId
    }

    @Test
    fun `NeventasEventTag returns proper JsonArray tag`() {
        val nevent = Nevent(
            userId = "userId",
            eventId = "eventId",
            kind = NostrEventKind.Highlight.value,
            relays = listOf("relay"),
        )
        val expectedMarker = ""

        val actual = nevent.asEventTag(marker = null)

        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 5
        actual[0].jsonPrimitive.content shouldBe "e"
        actual[1].jsonPrimitive.content shouldBe nevent.eventId
        actual[2].jsonPrimitive.content shouldBe nevent.relays[0]
        actual[3].jsonPrimitive.content shouldBe expectedMarker
        actual[4].jsonPrimitive.content shouldBe nevent.userId
    }

    @Test
    fun `NeventasEventTag returns proper JsonArray tag if optional args null`() {
        val nevent = Nevent(
            userId = "userId",
            eventId = "eventId",
            kind = NostrEventKind.Highlight.value,
            relays = emptyList(),
        )
        val expectedMarker = ""
        val expectedRelay = ""

        val actual = nevent.asEventTag(marker = null)

        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 5
        actual[0].jsonPrimitive.content shouldBe "e"
        actual[1].jsonPrimitive.content shouldBe nevent.eventId
        actual[2].jsonPrimitive.content shouldBe expectedRelay
        actual[3].jsonPrimitive.content shouldBe expectedMarker
        actual[4].jsonPrimitive.content shouldBe nevent.userId
    }

    @Test
    fun `asPubkeyTag returns proper JsonArray tag`() {
        val pubkey = "myPubkey"
        val expectedRecommendedRelay = "relay"
        val expectedOptional = "marker"
        val actual = pubkey.asPubkeyTag(
            relayHint = expectedRecommendedRelay,
            optional = expectedOptional,
        )
        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 4
        actual[0].jsonPrimitive.content shouldBe "p"
        actual[1].jsonPrimitive.content shouldBe pubkey
        actual[2].jsonPrimitive.content shouldBe expectedRecommendedRelay
        actual[3].jsonPrimitive.content shouldBe expectedOptional
    }

    @Test
    fun `asPubkeyTag returns proper JsonArray tag if optional args null`() {
        val pubkey = "myPubkey"
        val expectedRecommendedRelay = ""
        val expectedOptional = ""
        val actual = pubkey.asPubkeyTag(
            relayHint = null,
            optional = null,
        )
        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 4
        actual[0].jsonPrimitive.content shouldBe "p"
        actual[1].jsonPrimitive.content shouldBe pubkey
        actual[2].jsonPrimitive.content shouldBe expectedRecommendedRelay
        actual[3].jsonPrimitive.content shouldBe expectedOptional
    }

    @Test
    fun `NeventasPubkeyTag returns proper JsonArray tag`() {
        val nevent = Nevent(
            userId = "userId",
            eventId = "eventId",
            kind = NostrEventKind.Highlight.value,
            relays = listOf("relay"),
        )
        val expectedRecommendedRelay = "relay"
        val expectedOptional = "marker"
        val actual = nevent.asPubkeyTag(
            marker = expectedOptional,
        )
        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 4
        actual[0].jsonPrimitive.content shouldBe "p"
        actual[1].jsonPrimitive.content shouldBe nevent.userId
        actual[2].jsonPrimitive.content shouldBe expectedRecommendedRelay
        actual[3].jsonPrimitive.content shouldBe expectedOptional
    }

    @Test
    fun `NeventasPubkeyTag returns proper JsonArray tag if optional args null`() {
        val nevent = Nevent(
            userId = "userId",
            eventId = "eventId",
            kind = NostrEventKind.Highlight.value,
            relays = emptyList(),
        )
        val expectedRecommendedRelay = ""
        val expectedOptional = ""
        val actual = nevent.asPubkeyTag(
            marker = null,
        )
        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 4
        actual[0].jsonPrimitive.content shouldBe "p"
        actual[1].jsonPrimitive.content shouldBe nevent.userId
        actual[2].jsonPrimitive.content shouldBe expectedRecommendedRelay
        actual[3].jsonPrimitive.content shouldBe expectedOptional
    }

    @Test
    fun `NaddrasPubkeyTag returns proper JsonArray tag`() {
        val naddr = Naddr(
            userId = "userId",
            identifier = "identifier",
            kind = NostrEventKind.LongFormContent.value,
            relays = listOf("relay"),
        )
        val expectedRecommendedRelay = "relay"
        val expectedOptional = "marker"
        val actual = naddr.asPubkeyTag(
            marker = expectedOptional,
        )
        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 4
        actual[0].jsonPrimitive.content shouldBe "p"
        actual[1].jsonPrimitive.content shouldBe naddr.userId
        actual[2].jsonPrimitive.content shouldBe expectedRecommendedRelay
        actual[3].jsonPrimitive.content shouldBe expectedOptional
    }

    @Test
    fun `NaddrasPubkeyTag returns proper JsonArray tag if optional args null`() {
        val naddr = Naddr(
            userId = "userId",
            identifier = "identifier",
            kind = NostrEventKind.LongFormContent.value,
            relays = emptyList(),
        )
        val expectedRecommendedRelay = ""
        val expectedOptional = ""
        val actual = naddr.asPubkeyTag(
            marker = null,
        )
        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 4
        actual[0].jsonPrimitive.content shouldBe "p"
        actual[1].jsonPrimitive.content shouldBe naddr.userId
        actual[2].jsonPrimitive.content shouldBe expectedRecommendedRelay
        actual[3].jsonPrimitive.content shouldBe expectedOptional
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
    fun `asReplaceableEventTag returns proper JsonArray tag`() {
        val identifier = "identifier"
        val expectedRelayHint = "relay"
        val expectedMarker = "marker"

        val actual = identifier.asReplaceableEventTag(
            relayHint = expectedRelayHint,
            marker = expectedMarker,
        )

        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 4
        actual[0].jsonPrimitive.content shouldBe "a"
        actual[1].jsonPrimitive.content shouldBe identifier
        actual[2].jsonPrimitive.content shouldBe expectedRelayHint
        actual[3].jsonPrimitive.content shouldBe expectedMarker
    }

    @Test
    fun `asReplaceableEventTag returns proper JsonArray tag if optional args null`() {
        val identifier = "identifier"
        val expectedRelayHint = ""
        val expectedMarker = ""

        val actual = identifier.asReplaceableEventTag(
            relayHint = null,
            marker = null,
        )

        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 4
        actual[0].jsonPrimitive.content shouldBe "a"
        actual[1].jsonPrimitive.content shouldBe identifier
        actual[2].jsonPrimitive.content shouldBe expectedRelayHint
        actual[3].jsonPrimitive.content shouldBe expectedMarker
    }

    @Test
    fun `NaddrasReplaceableEventTag returns proper JsonArray tag`() {
        val naddr = Naddr(
            userId = "userId",
            identifier = "identifier",
            kind = NostrEventKind.LongFormContent.value,
            relays = listOf("relay"),
        )
        val expectedRelayHint = "relay"
        val expectedMarker = "marker"

        val actual = naddr.asReplaceableEventTag(
            marker = expectedMarker,
        )

        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 4
        actual[0].jsonPrimitive.content shouldBe "a"
        actual[1].jsonPrimitive.content shouldBe naddr.asATagValue()
        actual[2].jsonPrimitive.content shouldBe expectedRelayHint
        actual[3].jsonPrimitive.content shouldBe expectedMarker
    }

    @Test
    fun `NaddrasReplaceableEventTag returns proper JsonArray tag if optional args null`() {
        val naddr = Naddr(
            userId = "userId",
            identifier = "identifier",
            kind = NostrEventKind.LongFormContent.value,
            relays = emptyList(),
        )
        val expectedRelayHint = ""
        val expectedMarker = ""

        val actual = naddr.asReplaceableEventTag(
            marker = null,
        )

        actual shouldBe instanceOf(JsonArray::class)
        actual.size shouldBe 4
        actual[0].jsonPrimitive.content shouldBe "a"
        actual[1].jsonPrimitive.content shouldBe naddr.asATagValue()
        actual[2].jsonPrimitive.content shouldBe expectedRelayHint
        actual[3].jsonPrimitive.content shouldBe expectedMarker
    }

    @Test
    fun `parseEventTags returns empty tags`() {
        val content = "this is some content without any tags"

        val actual = content.parseEventTags()
        actual.shouldBeEmpty()
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
    fun `parseEventTags returns single tag for repeating nostrnevent`() {
        val nostrNevent = "nostr:nevent1qqs0ejzkvqdlqaej8k7edamkvzcnjv77u6npmp2qhdpv" +
            "swyjvcplafqpp4mhxue69uhkummn9ekx7mqzyrhxagf6h8l9cjngatumrg60uq2" +
            "2v66qz979pm32v985ek54ndh8gqcyqqqqqqgpldx8x"

        val content = "some content\n\n$nostrNevent $nostrNevent"

        val actual = content.parseEventTags()
        actual.size shouldBe 1
        val actualFirst = actual.firstOrNull()
        actualFirst.shouldNotBeNull()
        actualFirst[0].jsonPrimitive.content shouldBe "e"
        actualFirst[1].jsonPrimitive.content shouldBe "fcc856601bf077323dbd96f77660b13933dee6a61d8540bb42c838926603fea4"
        actualFirst[2].jsonPrimitive.content shouldBe "wss://nos.lol"
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
    fun `parseEventTags returns single tags for repeating nostrnote`() {
        val nostrNote = "nostr:note1zwwmjqpaymqhjlq0tjezcyhpcx0hyh4m3u5p54tm7mmfxaqg03pq3w3d6g"
        val content = "this is some content $nostrNote $nostrNote"
        val actual = content.parseEventTags()
        actual.size shouldBe 1
        val actualFirst = actual.firstOrNull()
        actualFirst.shouldNotBeNull()
        actualFirst[0].jsonPrimitive.content shouldBe "e"
        actualFirst[1].jsonPrimitive.content shouldBe "139db9003d26c1797c0f5cb22c12e1c19f725ebb8f281a557bf6f69374087c42"
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
    fun `parseEventTags returns single tag for repeating note`() {
        val content = "note1zwwmjqpaymqhjlq0tjezcyhpcx0hyh4m3u5p54tm7mmfxaqg03pq3w3d6g"
        val actual = content.parseEventTags().firstOrNull()
        actual.shouldNotBeNull()
        actual[0].jsonPrimitive.content shouldBe "e"
        actual[1].jsonPrimitive.content shouldBe "139db9003d26c1797c0f5cb22c12e1c19f725ebb8f281a557bf6f69374087c42"
    }

    @Test
    fun `parseEventTags returns tags for nevent`() {
        val content = "nevent1qqs0ejzkvqdlqaej8k7edamkvzcnjv77u6npmp2qhdpv" +
            "swyjvcplafqpp4mhxue69uhkummn9ekx7mqzyrhxagf6h8l9cjngatumrg60uq2" +
            "2v66qz979pm32v985ek54ndh8gqcyqqqqqqgpldx8x"
        val actual = content.parseEventTags().firstOrNull()
        actual.shouldNotBeNull()
        actual[0].jsonPrimitive.content shouldBe "e"
        actual[1].jsonPrimitive.content shouldBe "fcc856601bf077323dbd96f77660b13933dee6a61d8540bb42c838926603fea4"
        actual[2].jsonPrimitive.content shouldBe "wss://nos.lol"
    }

    @Test
    fun `parseEventTags returns single tag for repeating nevent`() {
        val nevent = "nevent1qqs0ejzkvqdlqaej8k7edamkvzcnjv77u6npmp2qhdpv" +
            "swyjvcplafqpp4mhxue69uhkummn9ekx7mqzyrhxagf6h8l9cjngatumrg60uq2" +
            "2v66qz979pm32v985ek54ndh8gqcyqqqqqqgpldx8x"
        val content = "this is some content $nevent $nevent"
        val actual = content.parseEventTags()
        actual.size shouldBe 1
        val actualFirst = actual.firstOrNull()
        actualFirst.shouldNotBeNull()
        actualFirst[0].jsonPrimitive.content shouldBe "e"
        actualFirst[1].jsonPrimitive.content shouldBe "fcc856601bf077323dbd96f77660b13933dee6a61d8540bb42c838926603fea4"
        actualFirst[2].jsonPrimitive.content shouldBe "wss://nos.lol"
    }

    @Test
    fun `parseHashtagTags returns empty tags`() {
        val content = "This is a content without any hashtag so the list should be empty!"
        val actual = content.parseHashtagTags()
        actual.shouldBeEmpty()
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
    fun `parsePubkeyTags returns empty tags`() {
        val content = "This is some content without any profile mention"
        val actual = content.parsePubkeyTags()
        actual.shouldBeEmpty()
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
    fun `parsePubkeyTags returns single tag for repeating nostrnprofile`() {
        val nostrNProfile = "nostr:nprofile1qqsrhuxx8l9ex335q7he0f09aej04zpazpl" +
            "0ne2cgukyawd24mayt8gpp4mhxue69uhhytnc9e3k7mgpz4mhxue69uh" +
            "kg6nzv9ejuumpv34kytnrdaksjlyr9p"

        val content = "this is some content $nostrNProfile $nostrNProfile"

        val actual = content.parsePubkeyTags()
        actual.size shouldBe 1
        val actualFirst = actual.firstOrNull()
        actualFirst.shouldNotBeNull()
        actualFirst[0].jsonPrimitive.content shouldBe "p"
        actualFirst[1].jsonPrimitive.content shouldBe "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d"
        actualFirst[2].jsonPrimitive.content shouldBe "wss://r.x.com"
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
    fun `parsePubkeyTags returns single tag for repeating nprofile`() {
        val nProfile = "nprofile1qqsrhuxx8l9ex335q7he0f09aej04zpazpl0ne2cgukyawd24mayt" +
            "8gpp4mhxue69uhhytnc9e3k7mgpz4mhxue69uhkg6nzv9ejuumpv34kytnrdaksjlyr9p"
        val content = "this is some content $nProfile $nProfile"
        val actual = content.parsePubkeyTags()
        actual.size shouldBe 1

        val actualFirst = actual.firstOrNull()
        actualFirst.shouldNotBeNull()
        actualFirst[0].jsonPrimitive.content shouldBe "p"
        actualFirst[1].jsonPrimitive.content shouldBe "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d"
        actualFirst[2].jsonPrimitive.content shouldBe "wss://r.x.com"
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
    fun `parsePubkeyTags returns single tag for repeating nostrnpub`() {
        val nostrNPub = "nostr:npub10elfcs4fr0l0r8af98jlmgdh9c8tcxjvz9qkw038js35mp4dma8qzvjptg"
        val content = "this is some content $nostrNPub $nostrNPub"

        val actual = content.parsePubkeyTags()
        actual.size shouldBe 1

        val actualFirst = actual.firstOrNull()
        actualFirst.shouldNotBeNull()
        actualFirst[0].jsonPrimitive.content shouldBe "p"
        actualFirst[1].jsonPrimitive.content shouldBe "7e7e9c42a91bfef19fa929e5fda1b72e0ebc1a4c1141673e2794234d86addf4e"
    }

    @Test
    fun `parsePubkeyTags returns tags for npub`() {
        val content = "npub10elfcs4fr0l0r8af98jlmgdh9c8tcxjvz9qkw038js35mp4dma8qzvjptg"
        val actual = content.parsePubkeyTags().firstOrNull()
        actual.shouldNotBeNull()
        actual[0].jsonPrimitive.content shouldBe "p"
        actual[1].jsonPrimitive.content shouldBe "7e7e9c42a91bfef19fa929e5fda1b72e0ebc1a4c1141673e2794234d86addf4e"
    }

    @Test
    fun `parsePubkeyTags returns single tag for repeating npub`() {
        val npub = "npub10elfcs4fr0l0r8af98jlmgdh9c8tcxjvz9qkw038js35mp4dma8qzvjptg"
        val content = "this is some content $npub $npub"
        val actual = content.parsePubkeyTags()
        actual.size shouldBe 1
        val actualFirst = actual.firstOrNull()
        actualFirst.shouldNotBeNull()
        actualFirst[0].jsonPrimitive.content shouldBe "p"
        actualFirst[1].jsonPrimitive.content shouldBe "7e7e9c42a91bfef19fa929e5fda1b72e0ebc1a4c1141673e2794234d86addf4e"
    }

    @Test
    fun `parseReplaceableEventTags returns empty`() {
        val content = "this is some content without replaceable events"
        val actual = content.parseReplaceableEventTags()
        actual.shouldBeEmpty()
    }

    @Test
    fun `parseReplaceableEventTags returns tag for nostrnaddr`() {
        val naddr = Naddr(
            identifier = "Shipping-Shipyard-DVM-wey3m4",
            relays = listOf("wss://relay.damus.io", "wss://relay.primal.net"),
            userId = "fa984bd7dbb282f07e16e7ae87b26a2a7b9b90b7246a44771f0cf5ae58018f52",
            kind = 30023,
        )

        val actual = ("nostr:" + naddr.toNaddrString()).parseReplaceableEventTags()
        val actualFirst = actual.firstOrNull()
        actualFirst.shouldNotBeNull()
        actualFirst[0].jsonPrimitive.content shouldBe "a"
        actualFirst[1].jsonPrimitive.content shouldBe naddr.asATagValue()
        actualFirst[2].jsonPrimitive.content shouldBe naddr.relays.first()
    }

    @Test
    fun `parseReplaceableEventTags returns single tag for repeating nostrnaddr`() {
        val naddr = Naddr(
            identifier = "Shipping-Shipyard-DVM-wey3m4",
            relays = listOf("wss://relay.damus.io", "wss://relay.primal.net"),
            userId = "fa984bd7dbb282f07e16e7ae87b26a2a7b9b90b7246a44771f0cf5ae58018f52",
            kind = 30023,
        )
        val nostrNaddr = "nostr:" + naddr.toNaddrString()
        val content = "this is some content $nostrNaddr $nostrNaddr"

        val actual = content.parseReplaceableEventTags()

        actual.size shouldBe 1
        val actualFirst = actual.firstOrNull()
        actualFirst.shouldNotBeNull()
        actualFirst[0].jsonPrimitive.content shouldBe "a"
        actualFirst[1].jsonPrimitive.content shouldBe naddr.asATagValue()
        actualFirst[2].jsonPrimitive.content shouldBe naddr.relays.first()
    }

    @Test
    fun `parseReplaceableEventTags returns tag for naddr`() {
        val naddr = Naddr(
            identifier = "Shipping-Shipyard-DVM-wey3m4",
            relays = listOf("wss://relay.damus.io", "wss://relay.primal.net"),
            userId = "fa984bd7dbb282f07e16e7ae87b26a2a7b9b90b7246a44771f0cf5ae58018f52",
            kind = 30023,
        )

        val actual = naddr.toNaddrString().parseReplaceableEventTags()
        val actualFirst = actual.firstOrNull()
        actualFirst.shouldNotBeNull()
        actualFirst[0].jsonPrimitive.content shouldBe "a"
        actualFirst[1].jsonPrimitive.content shouldBe naddr.asATagValue()
        actualFirst[2].jsonPrimitive.content shouldBe naddr.relays.first()
    }

    @Test
    fun `parseReplaceableEventTags returns single tag for repeating naddr`() {
        val naddr = Naddr(
            identifier = "Shipping-Shipyard-DVM-wey3m4",
            relays = listOf("wss://relay.damus.io", "wss://relay.primal.net"),
            userId = "fa984bd7dbb282f07e16e7ae87b26a2a7b9b90b7246a44771f0cf5ae58018f52",
            kind = 30023,
        )
        val content = "this is some content ${naddr.toNaddrString()} ${naddr.toNaddrString()}"

        val actual = content.parseReplaceableEventTags()

        actual.size shouldBe 1
        val actualFirst = actual.firstOrNull()
        actualFirst.shouldNotBeNull()
        actualFirst[0].jsonPrimitive.content shouldBe "a"
        actualFirst[1].jsonPrimitive.content shouldBe naddr.asATagValue()
        actualFirst[2].jsonPrimitive.content shouldBe naddr.relays.first()
    }

    private fun createNoteAttachment(
        uri: Uri = Uri.EMPTY,
        remoteUrl: String? = "https://uploads.primal.net/image.jpg",
        mimeType: String? = null,
        originalHash: String? = null,
        uploadedHash: String? = null,
        sizeInBytes: Int? = null,
        dimensionInPixels: String? = null,
        uploadError: Throwable? = null,
    ): NoteAttachment {
        return NoteAttachment(
            localUri = uri,
            remoteUrl = remoteUrl,
            mimeType = mimeType,
            originalHash = originalHash,
            uploadedHash = uploadedHash,
            uploadedSizeInBytes = sizeInBytes,
            dimensionInPixels = dimensionInPixels,
            uploadError = uploadError,
        )
    }

    @Test
    fun createsIMetaTag_withMimeTypeIfAvailable() {
        createNoteAttachment(mimeType = "image/jpeg").asIMetaTag()
            .shouldContain(JsonPrimitive("m image/jpeg"))
    }

    @Test
    fun createsIMetaTag_withDimensionsIfAvailable() {
        createNoteAttachment(dimensionInPixels = "100x200").asIMetaTag()
            .shouldContain(JsonPrimitive("dim 100x200"))
    }

    @Test
    fun createsIMetaTag_withSizeInBytesIfAvailable() {
        createNoteAttachment(sizeInBytes = 6425281).asIMetaTag()
            .shouldContain(JsonPrimitive("size 6425281"))
    }

    @Test
    fun createsIMetaTag_withOriginalHashIfAvailable() {
        createNoteAttachment(originalHash = "original").asIMetaTag()
            .shouldContain(JsonPrimitive("ox original"))
    }

    @Test
    fun createsIMetaTag_withUploadedHashIfAvailable() {
        createNoteAttachment(uploadedHash = "uploaded").asIMetaTag()
            .shouldContain(JsonPrimitive("x uploaded"))
    }
}

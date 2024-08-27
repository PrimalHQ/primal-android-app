package net.primal.android.user.accounts

import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import org.junit.Test

class ParsersTest {

    private fun createRelayJsonArray(
        url: String,
        read: Boolean = true,
        write: Boolean = true,
    ): JsonArray {
        return buildJsonArray {
            add("r")
            add(url)
            when {
                read && write -> Unit
                read -> add("read")
                write -> add("write")
            }
        }
    }

    @Test
    fun parseKind3Relays_returnsEmptyListForEmptyContent() {
        "".parseKind3Relays() shouldBe emptyList()
    }

    @Test
    fun parseKind3Relays_returnsEmptyListForInvalidJsonContent() {
        "{ giberish }".parseKind3Relays() shouldBe emptyList()
    }

    @Test
    fun parseKind3Relays_readsRelayUrlReadAndWriteFields() {
        val expectedUrl = "wss://relay.primal.net"
        val expectedRead = false
        val expectedWrite = true
        val content = """
        {
            "$expectedUrl": {
                "read": $expectedRead,
                "write": $expectedWrite
            },
            "wss://random.primal.net": {
                "read": true,
                "write": true
            }
        }
        """
        val actual = content.parseKind3Relays()

        actual.shouldNotBeNull()
        actual.shouldNotBeEmpty()
        actual.size shouldBe 2
        actual.first().url shouldBe expectedUrl
        actual.first().read shouldBe expectedRead
        actual.first().write shouldBe expectedWrite
    }

    @Test
    fun parseNip65Relays_returnsEmptyListForEmptyTags() {
        listOf<JsonArray>().parseNip65Relays() shouldBe emptyList()
    }

    @Test
    fun parseNip65Relays_returnsEmptyListForInvalidTags() {
        listOf(
            buildJsonArray { add("random") },
            buildJsonArray { add("invalid") },
            buildJsonArray { add("words") },
        ).parseNip65Relays() shouldBe emptyList()
    }

    @Test
    fun parseNip65Relays_readsRelayUrlReadAndWriteFields() {
        val actualRelays = listOf(
            createRelayJsonArray("wss://relay.primal.net"),
            createRelayJsonArray("wss://relay.damus.io", read = true, write = false),
            createRelayJsonArray("wss://relay.bitcoin.org", read = false, write = true),
        ).parseNip65Relays()

        actualRelays.shouldNotBeNull()
        actualRelays.size shouldBe 3
        actualRelays[0].url shouldBe "wss://relay.primal.net"
        actualRelays[0].read shouldBe true
        actualRelays[0].write shouldBe true

        actualRelays[1].url shouldBe "wss://relay.damus.io"
        actualRelays[1].read shouldBe true
        actualRelays[1].write shouldBe false

        actualRelays[2].url shouldBe "wss://relay.bitcoin.org"
        actualRelays[2].read shouldBe false
        actualRelays[2].write shouldBe true
    }

    @Test
    fun `parseFollowings returns empty list for no content`() {
        emptyList<JsonArray>().parseFollowings() shouldBe emptySet()
    }

    @Test
    fun `parseFollowings ignores non-p tags`() {
        listOf(
            buildJsonArray {
                add("t")
                add("#bitcoin")
            },
            buildJsonArray {
                add("r")
                add("random")
            },
            buildJsonArray {
                add("x")
                add("extreme")
            },
        ).parseFollowings() shouldBe emptySet()
    }

    @Test
    fun `parseFollowings parses only p tags`() {
        val tags = listOf(
            buildJsonArray {
                add("p")
                add("invalidNpub")
            },
            buildJsonArray {
                add("p")
                add("invalidNpub2")
            },
            buildJsonArray {
                add("p")
                add("invalidNpub3")
            },
        )
        val actual = tags.parseFollowings()
        actual.shouldNotBeEmpty()
        actual.size shouldBe tags.size
    }

    @Test
    fun `parseInterests returns empty list for no content`() {
        emptyList<JsonArray>().parseInterests() shouldBe emptyList()
    }

    @Test
    fun `parseInterests ignores non-t tags`() {
        listOf(
            buildJsonArray {
                add("p")
                add("bitcoin")
            },
            buildJsonArray {
                add("r")
                add("random")
            },
            buildJsonArray {
                add("x")
                add("extreme")
            },
        ).parseInterests() shouldBe emptyList()
    }

    @Test
    fun `parseInterests parses only t tags`() {
        val tags = listOf(
            buildJsonArray {
                add("t")
                add("#bitcoin")
            },
            buildJsonArray {
                add("t")
                add("#nostr")
            },
            buildJsonArray {
                add("t")
                add("#android")
            },
        )
        val actual = tags.parseInterests()
        actual.shouldNotBeEmpty()
        actual.size shouldBe tags.size
    }
}

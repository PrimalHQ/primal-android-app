package net.primal.android.user.accounts

import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import org.junit.Test

class ParsersTest {

    @Test
    fun `parseRelays returns empty list for empty content`() {
        "".parseRelays() shouldBe emptyList()
    }

    @Test
    fun `parseRelays returns empty list for invalid json content`() {
        "{ giberish }".parseRelays() shouldBe emptyList()
    }

    @Test
    fun `parseRelays reads relay url, read and write`() {
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
        val actual = content.parseRelays()

        actual.shouldNotBeEmpty()
        actual.size shouldBe 2
        actual.first().url shouldBe expectedUrl
        actual.first().read shouldBe expectedRead
        actual.first().write shouldBe expectedWrite
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

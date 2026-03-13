package net.primal.domain.nostr.utils

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.domain.nostr.NostrEvent

class HashtagUtilsTest {

    private fun buildNostrEvent(content: String, tags: List<JsonArray> = emptyList()) =
        NostrEvent(
            id = "",
            pubKey = "",
            sig = "",
            createdAt = 0,
            tags = tags,
            content = content,
            kind = 1,
        )

    @Test
    fun `nostrEvent parseHashtags returns all hashtags`() {
        val event = buildNostrEvent(content = "Hello #Nostr! #Bitcoin says hi!")
        val actual = event.parseHashtags()
        actual.size shouldBe 2
        actual.shouldContain("#Nostr")
        actual.shouldContain("#Bitcoin")
    }

    @Test
    fun `nostrEvent parseHashtags returns hashtags with special chars before or after the hashtag`() {
        val event = buildNostrEvent(
            content = "This hashtags in brackets (#Nostr, #Bitcoin, #Primal) should be fine!",
        )
        val actual = event.parseHashtags()
        actual.size shouldBe 3
        actual.shouldContain("#Nostr")
        actual.shouldContain("#Bitcoin")
        actual.shouldContain("#Primal")
    }

    @Test
    fun `nostrEvent parseHashtags does not return the hashtags for deprecated mentions`() {
        val event = buildNostrEvent(
            content = "This man #[1] and this one #[2] are part of #Nostr community!",
        )
        val actual = event.parseHashtags()
        actual.size shouldBe 1
        actual.shouldContain("#Nostr")
        actual.shouldNotContain("#[1]")
        actual.shouldNotContain("#[2]")
    }

    @Test
    fun `nostrEvent parseHashtags includes t hashtags from event tags`() {
        val event = buildNostrEvent(
            content = "This is #Nostr app!",
            tags = listOf(
                buildJsonArray {
                    add("t")
                    add("Bitcoin")
                },
                buildJsonArray {
                    add("t")
                    add("OpenSource")
                },
            ),
        )
        val actual = event.parseHashtags()
        actual.size shouldBe 3
        actual.shouldContain("#Nostr")
        actual.shouldContain("#Bitcoin")
        actual.shouldContain("#OpenSource")
    }

    @Test
    fun `string parseHashtags returns all hashtags`() {
        val content = "Hello #Nostr! #Bitcoin says hi!"
        val actual = content.parseHashtags()
        actual.size shouldBe 2
        actual.shouldContain("#Nostr")
        actual.shouldContain("#Bitcoin")
    }

    @Test
    fun `string parseHashtags returns hashtags with special chars before or after the hashtag`() {
        val content = "This hashtags in brackets (#Nostr, #Bitcoin, #Primal) should be fine!"
        val actual = content.parseHashtags()
        actual.size shouldBe 3
        actual.shouldContain("#Nostr")
        actual.shouldContain("#Bitcoin")
        actual.shouldContain("#Primal")
    }

    @Test
    fun `string parseHashtags does not return the hashtags for deprecated mentions`() {
        val content = "This man #[1] and this one #[2] are part of #Nostr community!"
        val actual = content.parseHashtags()
        actual.size shouldBe 1
        actual.shouldContain("#Nostr")
        actual.shouldNotContain("#[1]")
        actual.shouldNotContain("#[2]")
    }

    @Test
    fun `string parseHashtags does not return pure 1-2 digit numbers as hashtags`() {
        val content = "Step #1, then #2, and finally #24 are not hashtags. But #nostr is!"
        val actual = content.parseHashtags()
        actual.size shouldBe 1
        actual.shouldContain("#nostr")
        actual.shouldNotContain("#1")
        actual.shouldNotContain("#2")
        actual.shouldNotContain("#24")
    }

    @Test
    fun `string parseHashtags returns pure 3+ digit numbers as hashtags`() {
        val content = "Years like #2024 and #420 and #100 are valid hashtags"
        val actual = content.parseHashtags()
        actual.size shouldBe 3
        actual.shouldContain("#2024")
        actual.shouldContain("#420")
        actual.shouldContain("#100")
    }

    @Test
    fun `string parseHashtags returns mixed digit-letter hashtags`() {
        val content = "Tags like #2024btc and #1a are valid"
        val actual = content.parseHashtags()
        actual.size shouldBe 2
        actual.shouldContain("#2024btc")
        actual.shouldContain("#1a")
    }
}

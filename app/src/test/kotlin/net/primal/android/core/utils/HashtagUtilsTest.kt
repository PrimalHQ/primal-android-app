package net.primal.android.core.utils

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.nostr.model.NostrEvent
import org.junit.Test

class HashtagUtilsTest {

    private fun buildNostrEvent(
        content: String,
        tags: List<JsonArray> = emptyList()
    ) = NostrEvent(
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
            content = "This hashtags in brackets (#Nostr, #Bitcoin, #Primal) should be fine!"
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
            content = "This man #[1] and this one #[2] are part of #Nostr community!"
        )
        val actual = event.parseHashtags()
        actual.size shouldBe 1
        actual.shouldContain("#Nostr")
        actual.shouldNotContain("#[1]")
        actual.shouldNotContain("#[2]")
    }

    @Test
    fun `nostrEvent parseHashtags includes hashtags ('t') from event tags`() {
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
                }
            )
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

}

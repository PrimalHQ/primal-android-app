package net.primal.android.core.utils

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.Test

class TextMatcherTest {

    @Test
    fun `matches should match all known hashtags`() {
        val hashtags = listOf("#TextureTuesday", "#Texture", "#Details", "#Photography")
        val matcher = TextMatcher(
            content = "Rusty iron chain on gray gravel. #TextureTuesday #Texture #Details #Photography",
            texts = hashtags,
        )

        val actual = matcher.matches().map { it.value }
        actual.shouldContainAll(hashtags)
    }

    @Test
    fun `matches should match hashtags which are contained in other hashtags`() {
        val hashtags = listOf("#TextureTuesday", "#Texture", "#Zapathon", "#Zap")
        val matcher = TextMatcher(
            content = "Rusty iron chain on gray gravel. #TextureTuesday #Texture #Zap #Zapathon",
            texts = hashtags,
        )

        val actual = matcher.matches().map { it.value }
        actual.shouldContainAll(hashtags)
    }

    @Test
    fun `matches should match hashtags and return hashtags with duplicates`() {
        val hashtags = listOf("#nostr", "#bitcoin", "#sats", "#freedom")
        val expected = listOf("#nostr", "#nostr", "#nostr", "#bitcoin", "#freedom", "#freedom", "#sats")
        val matcher = TextMatcher(
            content = "Hello I love #nostr #nostr somemore #nostr #bitcoin #freedom #sats #freedom",
            texts = hashtags,
            repeatingOccurrences = true,
        )

        val actual = matcher.matches().map { it.value }
        actual.shouldContainExactlyInAnyOrder(expected)
    }

    @Test
    fun `matches should not match unknown hashtags`() {
        val hashtags = listOf("#Hiking", "#Trails")
        val matcher = TextMatcher(
            content = "The #Hiking Trails is available in Art prints with or without frame.",
            texts = hashtags,
        )

        val actual = matcher.matches().map { it.value }
        actual.shouldContain(hashtags.first())
        actual.shouldNotContain(hashtags.last())
    }
}

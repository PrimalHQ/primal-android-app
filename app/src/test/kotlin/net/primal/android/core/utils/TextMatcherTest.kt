package net.primal.android.core.utils

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldNotContain
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

    @Test
    fun `matches should prioritize longest matching hashtags first`() {
        val hashtags = listOf("#grownostr", "#artstr", "nostr", "art")
        val matcher = TextMatcher(
            content = "#art #nostr #artstr #grownostr",
            texts = hashtags,
        )

        val actual = matcher.matches().map { it.value }
        actual.shouldContainInOrder(hashtags.first())
    }

    @Test
    fun `matches should return an empty list when no hashtags match`() {
        val hashtags = listOf("#NotInText", "#AbsentTag")
        val matcher = TextMatcher(
            content = "This text contains no matching hashtags.",
            texts = hashtags,
        )

        val actual = matcher.matches()
        actual.shouldBeEmpty()
    }

    @Test
    fun `matches should correctly handle overlapping hashtags`() {
        val hashtags = listOf("#love", "#lovely", "#lovelyday")
        val matcher = TextMatcher(
            content = "What a #lovelyday to spread #love and enjoy a #lovely moment.",
            texts = hashtags,
        )

        val actual = matcher.matches().map { it.value }
        actual.shouldContainInOrder("#lovelyday", "#lovely", "#love")
    }

    @Test
    fun `matches should match single-character hashtags`() {
        val hashtags = listOf("#A", "#B", "#C")
        val matcher = TextMatcher(
            content = "Check out these: #A #B #C",
            texts = hashtags,
        )

        val actual = matcher.matches().map { it.value }
        actual.shouldContainAll("#A", "#B", "#C")
    }

    @Test
    fun `matches should match case-sensitive hashtags correctly`() {
        val hashtags = listOf("#Hello", "#HELLO", "#hello")
        val matcher = TextMatcher(
            content = "Greetings! #Hello #HELLO #hello",
            texts = hashtags,
        )

        val actual = matcher.matches().map { it.value }
        actual.shouldContainAll("#Hello", "#HELLO", "#hello")
    }

    @Test
    fun `matches should not match partial words`() {
        val hashtags = listOf("#car")
        val matcher = TextMatcher(
            content = "My favorite cartoon is not related to #car.",
            texts = hashtags,
        )

        val actual = matcher.matches().map { it.value }
        actual.shouldContain("#car")
    }

    @Test
    fun `matches should prioritize longest matching hashtags`() {
        val hashtags = listOf("#nostr", "#nostrich", "#nostrcommunity")
        val matcher = TextMatcher(
            content = "Join the #nostrcommunity for #nostrich updates on #nostr tech!",
            texts = hashtags,
        )

        val actual = matcher.matches().map { it.value }
        actual.shouldContainInOrder("#nostrcommunity", "#nostrich", "#nostr")
    }
}

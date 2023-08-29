package net.primal.android.core.utils

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import org.junit.Test

class HashtagMatcherTest {

    @Test
    fun `matches should match all known hashtags`() {
        val hashtags = listOf("#TextureTuesday", "#Texture", "#Details", "#Photography")
        val matcher = HashtagMatcher(
            content = "Rusty iron chain on gray gravel. #TextureTuesday #Texture #Details #Photography",
            hashtags = hashtags,
        )

        val actual = matcher.matches().map { it.value }
        actual.shouldContainAll(hashtags)
    }

    @Test
    fun `matches should match hashtags which are contained in other hashtags`() {
        val hashtags = listOf("#TextureTuesday", "#Texture", "#Zapathon", "#Zap")
        val matcher = HashtagMatcher(
            content = "Rusty iron chain on gray gravel. #TextureTuesday #Texture #Zap #Zapathon",
            hashtags = hashtags,
        )

        val actual = matcher.matches().map { it.value }
        actual.shouldContainAll(hashtags)
    }

    @Test
    fun `matches should not match unknown hashtags`() {
        val hashtags = listOf("#Hiking", "#Trails")
        val matcher = HashtagMatcher(
            content = "The #Hiking Trails is available in Art prints with or without frame.",
            hashtags = hashtags,
        )

        val actual = matcher.matches().map { it.value }
        actual.shouldContain(hashtags.first())
        actual.shouldNotContain(hashtags.last())
    }
}

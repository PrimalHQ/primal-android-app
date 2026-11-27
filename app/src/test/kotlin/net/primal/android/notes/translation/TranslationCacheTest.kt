package net.primal.android.notes.translation

import io.kotest.matchers.shouldBe
import org.junit.Test

class TranslationCacheTest {

    @Test
    fun `put and get round-trips`() {
        val cache = TranslationCache()
        cache.put("hello world this is a note", "https://example.com", "es", "hola mundo")
        cache.get("hello world this is a note", "https://example.com", "es") shouldBe "hola mundo"
        cache.count shouldBe 1
    }

    @Test
    fun `keys differ by target language and endpoint`() {
        val cache = TranslationCache()
        cache.put("some note content here", "https://a.example", "es", "uno")
        cache.get("some note content here", "https://a.example", "fr") shouldBe null
        cache.get("some note content here", "https://b.example", "es") shouldBe null
    }

    @Test
    fun `cache is keyed by api key as well`() {
        val cache = TranslationCache()
        cache.put("some note content here", "https://a.example", "es", "uno", apiKey = "key-one")
        cache.get("some note content here", "https://a.example", "es", apiKey = "key-two") shouldBe null
        cache.get("some note content here", "https://a.example", "es", apiKey = "key-one") shouldBe "uno"
        cache.get("some note content here", "https://a.example", "es") shouldBe null
    }

    @Test
    fun `evicts least recently used beyond max entries`() {
        val cache = TranslationCache(maxEntries = 2, maxChars = 1_000_000)
        cache.put("first note content", "https://example.com", "es", "primero")
        cache.put("second note content", "https://example.com", "es", "segundo")
        cache.put("third note content", "https://example.com", "es", "tercero")
        cache.count shouldBe 2
        cache.get("first note content", "https://example.com", "es") shouldBe null
        cache.get("third note content", "https://example.com", "es") shouldBe "tercero"
    }

    @Test
    fun `clear empties the cache`() {
        val cache = TranslationCache()
        cache.put("some note content here", "https://example.com", "es", "hola")
        cache.clear()
        cache.count shouldBe 0
        cache.get("some note content here", "https://example.com", "es") shouldBe null
    }

    @Test
    fun `character accounting stays in sync after count-based evictions`() {
        val cache = TranslationCache(maxEntries = 2, maxChars = 1_000)
        val value = "x".repeat(400)
        cache.put("first note content", "https://example.com", "es", value)
        cache.put("second note content", "https://example.com", "es", value)
        cache.put("third note content", "https://example.com", "es", value)
        cache.put("fourth note content", "https://example.com", "es", value)
        cache.count shouldBe 2
        cache.get("third note content", "https://example.com", "es") shouldBe value
        cache.get("fourth note content", "https://example.com", "es") shouldBe value
    }
}

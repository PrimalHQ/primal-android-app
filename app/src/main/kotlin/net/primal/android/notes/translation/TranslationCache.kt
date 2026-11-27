package net.primal.android.notes.translation

/**
 * Bounded in-memory cache for translation results.
 *
 * Translating is a network call to a (often self-hosted, rate-limited)
 * LibreTranslate instance, and feeds re-render the same notes constantly, so
 * results are cached by a hash of (endpoint, target language, API key,
 * content) — a credentials change must not serve results fetched under
 * the previous configuration.
 * The cache is bounded both by entry count and by total characters so it can
 * never grow without limit; least-recently-used entries are evicted first.
 *
 * Ported from the web implementation in PrimalHQ/primal-web-app
 * (src/lib/translation.ts, PR #240), where the cache was persisted to
 * localStorage; on Android it is kept in memory for the app session.
 */
class TranslationCache(
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
    private val maxChars: Int = DEFAULT_MAX_CHARS,
) {

    private val entries = object : LinkedHashMap<String, String>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>): Boolean {
            val shouldRemove = size > maxEntries
            if (shouldRemove) {
                // Keep the character accounting in sync with count-based eviction.
                totalChars -= eldest.value.length
            }
            return shouldRemove
        }
    }
    private var totalChars = 0

    val count: Int
        get() = synchronized(entries) { entries.size }

    fun get(content: String, endpoint: String, targetLanguage: String, apiKey: String = ""): String? {
        val key = buildCacheKey(content, endpoint, targetLanguage, apiKey)
        return synchronized(entries) { entries[key] }
    }

    fun put(content: String, endpoint: String, targetLanguage: String, translatedText: String, apiKey: String = "") {
        val key = buildCacheKey(content, endpoint, targetLanguage, apiKey)
        synchronized(entries) {
            val previous = entries.put(key, translatedText)
            totalChars += translatedText.length - (previous?.length ?: 0)
            evictToFit()
        }
    }

    fun clear() {
        synchronized(entries) {
            entries.clear()
            totalChars = 0
        }
    }

    private fun evictToFit() {
        val iterator = entries.entries.iterator()
        while (totalChars > maxChars && iterator.hasNext()) {
            val eldest = iterator.next()
            totalChars -= eldest.value.length
            iterator.remove()
        }
    }

    companion object {
        const val DEFAULT_MAX_ENTRIES = 200
        const val DEFAULT_MAX_CHARS = 1_000_000

        fun buildCacheKey(content: String, endpoint: String, targetLanguage: String, apiKey: String = ""): String {
            // Two independently seeded hashes plus the content length: a
            // single 53-bit hash collision must never serve a wrong
            // translation, and two seeds make that negligible.
            val input = "$endpoint|$targetLanguage|$apiKey|$content"
            val hash = cyrb53(input)
            val hash2 = cyrb53(input, seed = 1)
            return "${hash.toString(36)}.${hash2.toString(36)}:${content.length}"
        }

        /**
         * cyrb53 hash, ported from the web implementation so cache keys are
         * computed the same way. Not cryptographic — it only keys a local
         * cache of public note content.
         */
        fun cyrb53(text: String, seed: Int = 0): Long {
            var h1 = 0xdeadbeef.toInt() xor seed
            var h2 = 0x41c6ce57 xor seed
            for (ch in text) {
                val code = ch.code
                h1 = (h1 xor code) * 2654435761L.toInt()
                h2 = (h2 xor code) * 1597334677
            }
            h1 = (h1 xor (h1 ushr 16)) * 2246822507L.toInt()
            h1 = h1 xor ((h2 xor (h2 ushr 13)) * 3266489909L.toInt())
            h2 = (h2 xor (h2 ushr 16)) * 2246822507L.toInt()
            h2 = h2 xor ((h1 xor (h1 ushr 13)) * 3266489909L.toInt())
            return 4294967296L * (2097151 and h2) + (h1.toLong() and 0xFFFFFFFFL)
        }
    }
}

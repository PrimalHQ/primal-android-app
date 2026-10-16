package net.primal.android.notes.translation

class NoteTranslationCache(
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
) {
    private val entries = object : LinkedHashMap<CacheKey, NoteTranslationOutcome>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<CacheKey, NoteTranslationOutcome>?): Boolean {
            return size > maxEntries
        }
    }

    @Synchronized
    fun get(key: CacheKey): NoteTranslationOutcome? = entries[key]

    @Synchronized
    fun put(key: CacheKey, value: NoteTranslationOutcome) {
        if (value is NoteTranslationOutcome.Failed || value is NoteTranslationOutcome.NotConfigured) return
        entries[key] = value
    }

    @Synchronized
    fun clear() = entries.clear()

    data class CacheKey(
        val textHash: Int,
        val targetLanguage: String,
        val routeId: String,
    )

    companion object {
        const val DEFAULT_MAX_ENTRIES = 64
    }
}

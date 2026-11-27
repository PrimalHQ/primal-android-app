package net.primal.android.notes.translation

/**
 * Lightweight, offline language detection for note prose.
 *
 * This intentionally does NOT try to be a general-purpose detector. It first
 * checks for scripts that map (almost) unambiguously to a language, then
 * falls back to stopword scoring for Latin-script languages. It returns null
 * whenever it is not reasonably confident, and callers must treat null as
 * "unknown" — never as English.
 *
 * The detected code is only used to (a) hide the Translate action when a note
 * already appears to be in the user's target language and (b) label the
 * result ("Translated from Spanish"). The translation request itself always
 * uses source = "auto" on the provider.
 *
 * Ported from the web implementation in PrimalHQ/primal-web-app
 * (src/lib/languageDetect.ts, PR #240).
 */
object LanguageDetector {

    private val STOPWORDS: Map<String, List<String>> = mapOf(
        "en" to listOf(
            "the", "and", "is", "to", "of", "a", "in", "that", "it", "was", "for", "on", "are", "with", "as",
            "i", "his", "they", "be", "at", "one", "have", "this", "from", "or", "had", "by", "words", "but",
            "not", "what", "all", "were", "we", "when", "your", "can", "said", "there", "use", "an", "each",
            "which", "she", "do", "how", "their", "will",
        ),
        "es" to listOf(
            "el", "la", "de", "que", "y", "en", "un", "una", "los", "las", "del", "se", "no", "es", "por",
            "con", "su", "para", "como", "está", "más", "pero", "sus", "le", "ya", "o", "este", "sí",
            "porque", "cuando", "muy", "sin", "sobre", "también", "me", "hasta", "hay", "donde", "quien",
            "desde", "todo", "nos", "durante", "todos", "uno", "les", "ni", "contra", "otros", "ese",
        ),
        "pt" to listOf(
            "o", "a", "de", "e", "do", "da", "que", "em", "um", "para", "é", "com", "não", "uma", "os",
            "no", "se", "na", "por", "mais", "as", "dos", "como", "mas", "foi", "ao", "ele", "das", "tem",
            "à", "seu", "sua", "ou", "ser", "quando", "muito", "há", "nos", "já", "está", "eu", "também",
            "só", "pelo", "pela", "até", "isso", "ela",
        ),
        "fr" to listOf(
            "le", "la", "de", "des", "et", "les", "à", "un", "une", "du", "est", "dans", "que", "qui",
            "pour", "pas", "sur", "plus", "par", "au", "aux", "avec", "ce", "en", "et", "on", "se", "ne",
            "mais", "ou", "comme", "tout", "sont", "être", "aussi", "leur", "bien", "peut", "ces", "y",
            "deux", "même", "si", "mon", "sans", "nous",
        ),
        "de" to listOf(
            "der", "die", "und", "in", "den", "von", "zu", "mit", "sich", "des", "auf", "für", "ist",
            "im", "dem", "nicht", "ein", "eine", "als", "auch", "es", "an", "werden", "aus", "er", "hat",
            "dass", "sie", "nach", "wird", "bei", "einer", "um", "am", "sind", "noch", "wie", "einem",
            "über", "einen", "so", "zum", "haben", "nur", "oder", "aber", "vor",
        ),
        "it" to listOf(
            "il", "di", "che", "e", "la", "per", "un", "una", "in", "con", "del", "sono", "le", "è",
            "dei", "gli", "da", "lo", "come", "questo", "ma", "anche", "questa", "essere", "ci", "ne",
            "della", "nel", "al", "più", "se", "era", "si", "delle", "a", "lui", "lei", "io", "noi",
            "loro", "mio", "tuo", "suo", "nostro", "vostro", "cosa", "fare", "dire", "dove",
        ),
        "nl" to listOf(
            "de", "het", "een", "van", "ik", "te", "dat", "die", "in", "een", "hij", "het", "niet",
            "zijn", "is", "op", "aan", "met", "als", "voor", "zijn", "er", "maar", "om", "hem", "dan",
            "zou", "of", "wat", "mijn", "men", "dit", "zo", "door", "over", "ze", "zich", "bij", "ook",
            "tot", "je", "mij", "uit", "der", "daar", "haar", "naar", "heb", "hoe", "heeft",
        ),
        "id" to listOf(
            "yang", "dan", "di", "ini", "itu", "dengan", "untuk", "tidak", "dari", "dalam", "akan",
            "pada", "juga", "saya", "ke", "adalah", "oleh", "atau", "ada", "mereka", "sebagai", "telah",
            "sudah", "bisa", "kami", "kita", "dia", "ia", "sangat", "banyak", "lebih", "harus", "dapat",
            "tersebut", "sebuah", "karena", "saat", "tapi", "namun", "jadi", "bagi", "atas", "lagi",
            "masih", "kamu", "anda", "aku", "kalau", "jika", "bukan",
        ),
        "tr" to listOf(
            "bir", "ve", "bu", "da", "de", "için", "çok", "en", "o", "ne", "gibi", "daha", "ama",
            "kadar", "sonra", "önce", "nasıl", "niye", "ya", "ki", "mü", "ise", "olan", "olarak",
            "şey", "sen", "ben", "biz", "siz", "onlar", "bana", "sana", "ona", "bizi", "sizi", "onları",
            "kendi", "şu", "her", "hiç", "zaman", "yerde", "göre", "rağmen", "dolayı", "tarafından",
        ),
        "pl" to listOf(
            "i", "w", "nie", "się", "na", "to", "z", "jest", "że", "do", "tak", "jak", "co", "o", "ale",
            "po", "za", "tym", "od", "przez", "ich", "który", "być", "ten", "go", "już", "tylko", "dla",
            "podczas", "może", "przy", "kiedy", "bardzo", "też", "jako", "nad", "jeszcze", "przed",
            "bez", "tego", "lub", "aby", "wiele", "kilka", "coraz", "gdzie", "czy", "między",
        ),
    )

    private val WORD_SPLIT_RE = Regex("""[^\p{L}]+""")

    /** Best-effort ISO 639-1 code for [text], or null when not confident. */
    fun detectLanguage(text: String): String? {
        val sample = text.trim()
        if (sample.isEmpty()) return null

        detectByScript(sample)?.let { return it }
        return detectLatinByStopwords(sample)
    }

    private fun detectByScript(sample: String): String? {
        // Kana => Japanese; Hangul => Korean; Han alone defaults to Chinese.
        if (sample.any { it in '\u3040'..'\u30FF' }) return "ja"
        if (sample.any { it in '\uAC00'..'\uD7A3' || it in '\u1100'..'\u11FF' }) return "ko"
        if (sample.any { it in '\u0E00'..'\u0E7F' }) return "th"
        if (sample.any { it in '\u0900'..'\u097F' }) return "hi"
        if (sample.any { it in '\u0590'..'\u05FF' }) return "he"
        if (sample.any { it in '\u0600'..'\u06FF' }) return "ar"
        if (sample.any { it in '\u0370'..'\u03FF' }) return "el"
        if (sample.any { it in '\u0400'..'\u04FF' }) return detectCyrillic(sample)
        if (sample.any { it in '\u4E00'..'\u9FFF' }) return "zh"
        return null
    }

    private fun detectCyrillic(sample: String): String {
        // Letters distinctive to Ukrainian / Serbian; otherwise default to Russian.
        if (sample.any { it in "іїєґІЇЄҐ" }) return "uk"
        if (sample.any { it in "ђјљњћџЂЈЉЊЋЏ" }) return "sr"
        return "ru"
    }

    private fun detectLatinByStopwords(sample: String): String? {
        val words = sample.lowercase().split(WORD_SPLIT_RE).filter { it.isNotEmpty() }
        if (words.size < 4) return null

        var best: String? = null
        var bestScore = 0
        var secondScore = 0
        for ((code, stopwords) in STOPWORDS) {
            val stopwordSet = stopwords.toSet()
            val score = words.count { it in stopwordSet }
            if (score > bestScore) {
                secondScore = bestScore
                bestScore = score
                best = code
            } else if (score > secondScore) {
                secondScore = score
            }
        }
        // Require a clear winner with a handful of hits to avoid coin flips
        // between related languages on short notes.
        return if (best != null && bestScore >= 2 && bestScore > secondScore) best else null
    }

    /** 'en-US' -> 'en'; 'EN' -> 'en'. Used to compare locale-ish codes. */
    fun primaryLanguageCode(code: String?): String? {
        if (code.isNullOrBlank()) return null
        val primary = code.trim().lowercase().split('-', '_').firstOrNull()
        return if (primary.isNullOrEmpty()) null else primary
    }
}

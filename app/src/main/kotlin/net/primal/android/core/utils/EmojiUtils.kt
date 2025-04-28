package net.primal.android.core.utils

private val EMOJI_RANGES = arrayOf(
    0x1F300..0x1F5FF,
    0x1F600..0x1F64F,
    0x1F680..0x1F6FF,
    0x1F900..0x1F9FF,
    0x1FA70..0x1FAFF,
    0x2600..0x26FF,
    0x2700..0x27BF,
    0x1F1E6..0x1F1FF,
)

/* https://stackoverflow.com/a/79562963 */
@Suppress("ReturnCount", "NestedBlockDepth", "MagicNumber")
fun String.isOnlyEmoji(): Boolean {
    if (isEmpty()) return false

    var i = 0
    while (i < this.length) {
        val codePoint = codePointAt(i)

        if (!EMOJI_RANGES.any { range -> codePoint in range }) {
            // Check for surrogate pairs (like flags)
            if (Character.isHighSurrogate(this[i].toChar())) {
                if (i + 1 >= this.length) return false
                val fullCodePoint = Character.toCodePoint(this[i], this[i + 1])
                if (fullCodePoint !in 0x1F1E6..0x1F1FF) return false
                i++ // Skip low surrogate
            } else {
                return false
            }
        }
        i += Character.charCount(codePoint)
    }
    return true
}

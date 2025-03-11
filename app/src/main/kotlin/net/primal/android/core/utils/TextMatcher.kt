package net.primal.android.core.utils

class TextMatcher(
    content: String,
    texts: List<String>,
    repeatingOccurrences: Boolean = false,
) {

    private val matches: MutableList<TextMatch> = mutableListOf()

    fun matches(): List<TextMatch> = matches

    private fun containsIndex(index: Int) = matches.find { it.startIndex == index } != null

    private fun String.indexOfNotMatchedBefore(text: String): Int? {
        var startIndex = 0
        var foundIndex: Int? = -1
        while (foundIndex == -1) {
            val indexOf = this.indexOf(text, startIndex = startIndex)
            when {
                indexOf == -1 -> foundIndex = null
                containsIndex(index = indexOf) -> startIndex = indexOf + 1
                else -> foundIndex = indexOf
            }
        }
        return foundIndex
    }

    init {
        texts.sortedByDescending { it.length }.forEach { text ->
            var currentIndex = content.indexOfNotMatchedBefore(text = text)

            while (currentIndex != null) {
                matches.add(
                    TextMatch(
                        value = text,
                        startIndex = currentIndex,
                        endIndex = currentIndex + text.length,
                    ),
                )

                if (!repeatingOccurrences) break

                currentIndex = content.indexOfNotMatchedBefore(text = text)
            }
        }
    }
}

data class TextMatch(
    val value: String,
    val startIndex: Int,
    val endIndex: Int,
)

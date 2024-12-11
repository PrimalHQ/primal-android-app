package net.primal.android.core.utils

class TextMatcher(
    content: String,
    texts: List<String>,
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
        texts.forEach {
            val startIndex = content.indexOfNotMatchedBefore(text = it)
            if (startIndex != null) {
                matches.add(
                    TextMatch(
                        value = it,
                        startIndex = startIndex,
                        endIndex = startIndex + it.length,
                    ),
                )
            }
        }
    }
}

data class TextMatch(
    val value: String,
    val startIndex: Int,
    val endIndex: Int,
)

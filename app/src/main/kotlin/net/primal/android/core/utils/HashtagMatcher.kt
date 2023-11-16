package net.primal.android.core.utils

class HashtagMatcher(
    content: String,
    hashtags: List<String>,
) {

    private val matches: MutableList<HashtagMatch> = mutableListOf()
    fun matches(): List<HashtagMatch> = matches

    private fun containsIndex(index: Int) = matches.find { it.startIndex == index } != null

    private fun String.indexOfNotMatchedBefore(hashtag: String): Int? {
        var startIndex = 0
        var foundIndex: Int? = -1
        while (foundIndex == -1) {
            val indexOf = this.indexOf(hashtag, startIndex = startIndex)
            when {
                indexOf == -1 -> foundIndex = null
                containsIndex(index = indexOf) -> startIndex = indexOf + 1
                else -> foundIndex = indexOf
            }
        }
        return foundIndex
    }

    init {
        hashtags.forEach {
            val startIndex = content.indexOfNotMatchedBefore(hashtag = it)
            if (startIndex != null) {
                matches.add(
                    HashtagMatch(
                        value = it,
                        startIndex = startIndex,
                        endIndex = startIndex + it.length,
                    ),
                )
            }
        }
    }
}

data class HashtagMatch(
    val value: String,
    val startIndex: Int,
    val endIndex: Int,
)

package net.primal.domain.reads

private const val WordsPerMinute = 200

fun Int?.wordsCountToReadingTime() = ((this ?: 1) / WordsPerMinute) + 1

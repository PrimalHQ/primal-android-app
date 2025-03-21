package net.primal.domain.utils

private const val WordsPerMinute = 200

fun Int?.wordsCountToReadingTime() = ((this ?: 1) / WordsPerMinute) + 1

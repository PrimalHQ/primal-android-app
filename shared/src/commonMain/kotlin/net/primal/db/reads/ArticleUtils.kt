package net.primal.db.reads

private const val WordsPerMinute = 200

internal fun Int?.wordsCountToReadingTime() = ((this ?: 1) / WordsPerMinute) + 1

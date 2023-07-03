package net.primal.android.core.ext

fun String.isTrendingFeed(): Boolean = this.endsWith("trending")

fun String.isPopularFeed(): Boolean = this.endsWith("popular")

fun String.isMostZappedFeed(): Boolean = this.endsWith("mostzapped4h")

fun String.isLatestFeed(): Boolean = !isTrendingFeed() && !isPopularFeed() && !isMostZappedFeed()

fun String.isAuthoredFeed(): Boolean = this.startsWith("authored;")

fun String.removeAuthoredPrefix(): String = this.substring("authored;".length)

fun String.removeSearchPrefix(): String = this.substring("search;".length)

package net.primal.android.core.ext

fun String.isTrendingFeed(): Boolean = this.endsWith("trending")

fun String.isPopularFeed(): Boolean = this.endsWith("popular")

fun String.isMostZappedFeed(): Boolean = this.endsWith("mostzapped4h")

fun String.isChronologicalFeed(): Boolean =
    !isTrendingFeed() && !isPopularFeed() && !isMostZappedFeed() && !isBookmarkFeed()

fun String.isBookmarkFeed(): Boolean = this.startsWith("bookmarks;")

fun String.isSearchFeed(): Boolean = this.startsWith("search;")

fun String.removeSearchPrefix(): String =
    this.substring("search;".length)
        .removePrefix("\"")
        .removeSuffix("\"")

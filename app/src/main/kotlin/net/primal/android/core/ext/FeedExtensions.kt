@file:Suppress("TooManyFunctions")

package net.primal.android.core.ext

import net.primal.android.crypto.hexToNpubHrp

private fun String.isPrimalExploreFeed(ending: String): Boolean {
    return this == "follows;$ending" || this == "tribe;$ending" ||
        this == "network;$ending" || this == "global;$ending"
}

fun String.isExploreTrendingFeed(): Boolean = isPrimalExploreFeed(ending = "trending")

fun String.isExplorePopularFeed(): Boolean = isPrimalExploreFeed(ending = "popular")

fun String.isExploreMostZappedFeed(): Boolean = isPrimalExploreFeed(ending = "mostzapped")

fun String.isExploreLatestFeed(): Boolean = isPrimalExploreFeed(ending = "latest")

fun String.isExploreMostZapped4hFeed(): Boolean = isPrimalExploreFeed(ending = "mostzapped4h")

fun String.isChronologicalFeed(): Boolean {
    return isUserFeed() || isUserLwrFeed() || isUserAuthoredFeed() ||
        isUserAuthoredRepliesFeed() || isExploreLatestFeed()
}

fun String.hasReposts(): Boolean {
    return isUserFeed() || isUserLwrFeed() || isUserAuthoredFeed() || isUserAuthoredRepliesFeed()
}

fun String.hasUpwardsPagination(): Boolean {
    return isUserFeed() || isUserLwrFeed() || isUserAuthoredFeed() || isUserAuthoredRepliesFeed()
}

private fun String.isUserHexFeed(prefix: String? = null): Boolean {
    return when (prefix) {
        null -> this.isUserFeed()
        else -> {
            val hex = this.substringAfter(prefix)
            hex.isNotEmpty() && hex.isUserFeed()
        }
    }
}

fun String.isUserFeed() = runCatching { this@isUserFeed.hexToNpubHrp() }.isSuccess

fun String.isUserLwrFeed(): Boolean {
    return startsWith("withreplies;") && isUserHexFeed(prefix = "withreplies;")
}

fun String.isUserAuthoredRepliesFeed(): Boolean {
    return startsWith("authoredreplies;") && isUserHexFeed(prefix = "authoredreplies;")
}

fun String.isUserAuthoredFeed(): Boolean {
    return startsWith("authored;") && isUserHexFeed(prefix = "authored;")
}

fun String.isBookmarkFeed(): Boolean = this.startsWith("bookmarks;")

fun String.isSearchFeed(): Boolean = this.startsWith("search;")

fun String.removeSearchPrefix(): String =
    this.substring("search;".length)
        .removePrefix("\"")
        .removeSuffix("\"")

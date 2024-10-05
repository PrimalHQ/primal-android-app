package net.primal.android.feeds.domain

import net.primal.android.crypto.hexToNpubHrp

fun String.isUserNotesFeedSpec(): Boolean {
    return this == "{\"id\":\"latest\",\"kind\":\"notes\"}"
}

fun String.isUserNotesLwrFeedSpec(): Boolean {
    return this == "{\"id\":\"latest\",\"include_replies\":true,\"kind\":\"notes\"}"
}

private fun String?.isValidProfileId(): Boolean {
    return if (this != null) {
        runCatching { this@isValidProfileId.hexToNpubHrp() }.isSuccess
    } else {
        return false
    }
}

private fun String.isPubkeyFeedSpec(prefix: String, suffix: String): Boolean {
    val beforeProfileId = "$prefix,\"pubkey\":\""
    val afterProfileId = "\"$suffix"
    val profileId = runCatching {
        this.substring(
            startIndex = beforeProfileId.length,
            endIndex = this.length - afterProfileId.length,
        )
    }.getOrNull()
    return this.startsWith(beforeProfileId) && this.endsWith(afterProfileId) && profileId.isValidProfileId()
}

fun String.isProfileNotesFeedSpec(): Boolean {
    return isPubkeyFeedSpec(
        prefix = "{\"id\":\"feed\",\"kind\":\"notes\"",
        suffix = "}",
    )
}

fun String.isProfileAuthoredNotesFeedSpec(): Boolean {
    return isPubkeyFeedSpec(
        prefix = "{\"id\":\"feed\",\"kind\":\"notes\",\"notes\":\"authored\"",
        suffix = "}",
    )
}

fun String.isProfileAuthoredNoteRepliesFeedSpec(): Boolean {
    return isPubkeyFeedSpec(
        prefix = "{\"id\":\"feed\",\"include_replies\":true,\"kind\":\"notes\",\"notes\":\"authored\"",
        suffix = "}",
    )
}

fun String.supportsUpwardsNotesPagination(): Boolean {
    return isUserNotesFeedSpec() || isUserNotesLwrFeedSpec() || isProfileNotesFeedSpec() ||
        isProfileAuthoredNotesFeedSpec() || isProfileAuthoredNoteRepliesFeedSpec()
}

fun String.supportsNoteReposts() = supportsUpwardsNotesPagination()

fun String.isNotesBookmarkFeedSpec(): Boolean {
    return isPubkeyFeedSpec(
        prefix = "{\"id\":\"feed\",\"kind\":\"notes\",\"notes\":\"bookmarks\"",
        suffix = "}",
    )
}

fun String.isReadsBookmarkFeedSpec(): Boolean {
    return isPubkeyFeedSpec(
        prefix = "{\"id\":\"feed\",\"kind\":\"reads\",\"kinds\":[30023],\"notes\":\"bookmarks\"}",
        suffix = "}",
    )
}

fun String.resolveFeedSpecKind(): FeedSpecKind? {
    return when {
        this.isNotesFeedSpec() -> FeedSpecKind.Notes
        this.isReadsFeedSpec() -> FeedSpecKind.Reads
        else -> null
    }
}

fun String.isNotesFeedSpec(): Boolean = this.contains("\"kind\":\"notes\"") || this.contains("kind:1")

fun String.isReadsFeedSpec(): Boolean = this.contains("\"kind\":\"reads\"") || this.contains("kind:30023")

fun buildNotesBookmarksFeedSpec(userId: String): String =
    "{\"id\":\"feed\",\"kind\":\"notes\",\"notes\":\"bookmarks\",\"pubkey\":\"$userId\"}"

fun buildAdvancedSearchNotesFeedSpec(query: String): String = """{"id":"advsearch","query":"kind:1 $query"}"""

fun buildSimpleSearchNotesFeedSpec(query: String): String = """{"id":"search","kind":"notes","query":"$query"}"""

fun buildAdvancedSearchArticlesFeedSpec(query: String): String = """{"id":"advsearch","query":"kind:30023 $query"}"""

fun buildExploreMediaFeedSpec() = """{"id":"explore-media"}"""

fun String.extractTopicFromFeedSpec(): String? {
    val noteQueryStartIndex = this.indexOf("\"query\":\"kind:1 #")
    val articleQueryStartIndex = this.indexOf("\"query\":\"kind:30023 #")

    return if (noteQueryStartIndex != -1) {
        val noteTopicStartIndex = this.indexOf("#", startIndex = noteQueryStartIndex)
        val noteTopicEndIndex = this.indexOf("}", startIndex = noteQueryStartIndex) - 1
        this.substring(startIndex = noteTopicStartIndex, endIndex = noteTopicEndIndex)
    } else if (articleQueryStartIndex != -1) {
        val articleTopicStartIndex = this.indexOf("#", startIndex = articleQueryStartIndex)
        val articleTopicEndIndex = this.indexOf("}", startIndex = articleQueryStartIndex) - 1
        this.substring(startIndex = articleTopicStartIndex, endIndex = articleTopicEndIndex)
    } else {
        null
    }
}

fun String.isSearchFeedSpec(): Boolean = this.contains("\"id\":\"advsearch\"") || this.contains("\"id\":\"search\"")

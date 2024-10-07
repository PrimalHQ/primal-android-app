package net.primal.android.feeds.domain

import net.primal.android.core.utils.ellipsizeMiddle
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

private fun String.extractPubkeyFromFeedSpec(prefix: String? = null, suffix: String? = null): String? {
    val beforeProfileId = if (prefix != null) "$prefix,\"pubkey\":\"" else "\"pubkey\":\""
    val afterProfileId = if (suffix != null) "\"$suffix" else "\""
    return runCatching {
        this.substring(
            startIndex = beforeProfileId.length,
            endIndex = this.length - afterProfileId.length,
        )
    }.getOrNull()
}

private fun String.isPubkeyFeedSpec(prefix: String? = null, suffix: String? = null): Boolean {
    val beforeProfileId = if (prefix != null) "$prefix,\"pubkey\":\"" else "\"pubkey\":\""
    val afterProfileId = if (suffix != null) "\"$suffix" else "\""
    val profileId = this.extractPubkeyFromFeedSpec(prefix = prefix, suffix = suffix)
    val startsWithPrefix = if (prefix != null) this.startsWith(beforeProfileId) else true
    val endsWithSuffix = if (suffix != null) this.endsWith(afterProfileId) else true
    return startsWithPrefix && endsWithSuffix && profileId.isValidProfileId()
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

fun buildNotesBookmarksFeedSpec(userId: String): String =
    "{\"id\":\"feed\",\"kind\":\"notes\",\"notes\":\"bookmarks\",\"pubkey\":\"$userId\"}"

fun String.isNotesBookmarkFeedSpec(): Boolean {
    return isPubkeyFeedSpec(
        prefix = "{\"id\":\"feed\",\"kind\":\"notes\",\"notes\":\"bookmarks\"",
        suffix = "}",
    )
}

fun buildArticleBookmarksFeedSpec(userId: String): String =
    "{\"id\":\"feed\",\"kind\":\"reads\",\"kinds\":[30023],\"notes\":\"bookmarks\",\"pubkey\":\"$userId\"}"

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

fun String.resolveDefaultTitle(): String =
    runCatching {
        val topic = extractTopicFromFeedSpec()?.substringAfter("#")
        val advancedQuery = extractAdvancedSearchQuery()
        val simpleQuery = extractSimpleSearchQuery()
        val pubkey = extractPubkeyFromFeedSpec()
        when {
            topic != null -> "Topic: $topic"

            isAdvancedSearchFeedSpec() && advancedQuery != null -> "Search: $advancedQuery"

            isSimpleSearchFeedSpec() && simpleQuery != null -> "Search: $simpleQuery"

            pubkey != null -> "User: ${pubkey.hexToNpubHrp().ellipsizeMiddle(size = 8)}"

            else -> ""
        }
    }.getOrDefault(defaultValue = "")

fun String.resolveDefaultDescription(): String =
    runCatching {
        val topic = extractTopicFromFeedSpec()?.substringAfter("#")
        return when {
            topic != null -> if (isNotesFeedSpec()) {
                "All notes tagged with $topic"
            } else if (isReadsFeedSpec()) {
                "All reads tagged with $topic"
            } else {
                "Tagged with $topic"
            }

            isSearchFeedSpec() -> "Primal Saved Search"

            isPubkeyFeedSpec() -> if (isNotesFeedSpec()) {
                "Latest notes by user follows"
            } else if (isReadsFeedSpec()) {
                "Latest articles by user follows"
            } else {
                "Latest by user follows"
            }

            else -> ""
        }
    }.getOrDefault(defaultValue = "")

fun String.isNotesFeedSpec(): Boolean = this.contains("\"kind\":\"notes\"") || this.contains("kind:1")

fun String.isReadsFeedSpec(): Boolean = this.contains("\"kind\":\"reads\"") || this.contains("kind:30023")

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

fun String.extractAdvancedSearchQuery(): String? {
    val noteQueryField = "\"query\":\"kind:1"
    val noteQueryFieldStartIndex = this.indexOf(noteQueryField)
    val articleQueryField = "\"query\":\"kind:30023"
    val articleQueryFieldStartIndex = this.indexOf(articleQueryField)

    return if (noteQueryFieldStartIndex != -1) {
        val noteQueryStartIndex = noteQueryFieldStartIndex + noteQueryField.length
        val noteQueryEndIndex = this.indexOf("\"", startIndex = noteQueryStartIndex)
        this.substring(startIndex = noteQueryStartIndex, endIndex = noteQueryEndIndex)
    } else if (articleQueryFieldStartIndex != -1) {
        val articleQueryStartIndex = articleQueryFieldStartIndex + articleQueryField.length
        val articleQueryEndIndex = this.indexOf("\"", startIndex = articleQueryStartIndex)
        this.substring(startIndex = articleQueryStartIndex, endIndex = articleQueryEndIndex)
    } else {
        null
    }
}

fun String.extractSimpleSearchQuery(): String? {
    val queryField = "\"query\":\""
    val queryFieldStartIndex = this.indexOf(queryField)
    return if (queryFieldStartIndex != -1) {
        val queryStartIndex = queryFieldStartIndex + queryField.length
        val queryEndIndex = this.indexOf("\"", startIndex = queryStartIndex)
        this.substring(queryStartIndex, queryEndIndex)
    } else {
        null
    }
}

fun String.isSearchFeedSpec(): Boolean = isAdvancedSearchFeedSpec() || isSimpleSearchFeedSpec()

fun String.isAdvancedSearchFeedSpec() = this.contains("\"id\":\"advsearch\"")

fun String.isSimpleSearchFeedSpec() = this.contains("\"id\":\"search\"")

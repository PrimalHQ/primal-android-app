package net.primal.android.feeds.domain

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.primal.android.R
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
    "{\"id\":\"feed\",\"kind\":\"notes\",\"kinds\":[30023],\"notes\":\"bookmarks\",\"pubkey\":\"$userId\"}"

fun String.isReadsBookmarkFeedSpec(): Boolean {
    return isPubkeyFeedSpec(
        prefix = "{\"id\":\"feed\",\"kind\":\"notes\",\"kinds\":[30023],\"notes\":\"bookmarks\"}",
        suffix = "}",
    )
}

fun buildLatestNotesUserFeedSpec(userId: String) = """{"id":"feed","kind":"notes","pubkey":"$userId"}"""

fun String.resolveFeedSpecKind(): FeedSpecKind? {
    return when {
        this.isNotesFeedSpec() -> FeedSpecKind.Notes
        this.isReadsFeedSpec() -> FeedSpecKind.Reads
        else -> null
    }
}

@Composable
fun String.resolveDefaultTitle(profileName: String? = null): String =
    runCatching {
        val topic = extractTopicFromFeedSpec()?.substringAfter("#")
        val advancedQuery = extractAdvancedSearchQuery()
        val simpleQuery = extractSimpleSearchQuery()
        val pubkey = extractPubkeyFromFeedSpec()
        when {
            topic != null -> {
                stringResource(R.string.explore_feed_topic_feed_title, topic)
            }

            isAdvancedSearchFeedSpec() && advancedQuery != null -> {
                stringResource(R.string.explore_feed_advanced_search_feed_title)
            }

            isSimpleSearchFeedSpec() && simpleQuery != null -> {
                stringResource(R.string.explore_feed_simple_search_feed_title, simpleQuery)
            }

            pubkey != null -> {
                val name = profileName ?: pubkey.hexToNpubHrp().ellipsizeMiddle(size = 8)
                stringResource(R.string.explore_feed_user_feed_title, name)
            }

            else -> ""
        }
    }.getOrDefault(defaultValue = "")

@Composable
fun String.resolveDefaultDescription(): String =
    runCatching {
        val topic = extractTopicFromFeedSpec()?.substringAfter("#")
        return when {
            topic != null -> if (isNotesFeedSpec()) {
                stringResource(R.string.explore_feed_topic_notes_feed_description, topic)
            } else if (isReadsFeedSpec()) {
                stringResource(R.string.explore_feed_topic_reads_feed_description, topic)
            } else {
                stringResource(R.string.explore_feed_topic_feed_description, topic)
            }

            isSearchFeedSpec() -> {
                stringResource(R.string.explore_feed_search_feed_description)
            }

            isPubkeyFeedSpec() -> if (isNotesFeedSpec()) {
                stringResource(R.string.explore_feed_user_notes_feed_description)
            } else if (isReadsFeedSpec()) {
                stringResource(R.string.explore_feed_user_reads_feed_description)
            } else {
                stringResource(R.string.explore_feed_user_feed_description)
            }

            else -> ""
        }
    }.getOrDefault(defaultValue = "")

fun String.isNotesFeedSpec() = this.contains("\"kind\":\"notes\"") || this.contains("kind:1")

fun String.isReadsFeedSpec() = this.contains("\"kind\":\"reads\"") || this.contains("kind:30023")

fun buildAdvancedSearchNotesFeedSpec(query: String) = """{"id":"advsearch","query":"kind:1 $query"}"""

fun buildAdvancedSearchReadsFeedSpec(query: String) = """{"id":"advsearch","query":"kind:30023 $query"}"""

fun buildAdvancedSearchNotificationsFeedSpec(query: String) =
    """{"id":"advsearch","query":"kind:1 scope:mynotifications $query"}"""

fun buildSimpleSearchNotesFeedSpec(query: String) = """{"id":"search","kind":"notes","query":"$query"}"""

fun buildReadsTopicFeedSpec(hashtag: String) = """{"kind":"reads","topic":"${hashtag.substring(startIndex = 1)}"}"""

fun buildExploreMediaFeedSpec() = """{"id":"explore-media"}"""

fun String.extractTopicFromFeedSpec(): String? {
    val noteQueryStartIndex = this.indexOf("\"query\":\"kind:1 #")
    val articleQueryStartIndex = this.indexOf("\"query\":\"kind:30023 #")
    val articleTopicQueryPrefix = "{\"kind\":\"reads\",\"topic\":\""
    val articleTopicQueryStartIndex = this.indexOf(articleTopicQueryPrefix)

    return if (noteQueryStartIndex != -1) {
        val noteTopicStartIndex = this.indexOf("#", startIndex = noteQueryStartIndex)
        val noteTopicEndIndex = this.indexOf("}", startIndex = noteQueryStartIndex) - 1
        this.substring(startIndex = noteTopicStartIndex, endIndex = noteTopicEndIndex)
    } else if (articleQueryStartIndex != -1) {
        val articleTopicStartIndex = this.indexOf("#", startIndex = articleQueryStartIndex)
        val articleTopicEndIndex = this.indexOf("}", startIndex = articleQueryStartIndex) - 1
        this.substring(startIndex = articleTopicStartIndex, endIndex = articleTopicEndIndex)
    } else if (articleTopicQueryStartIndex != -1) {
        val articleTopicEndIndex = this.indexOf("}", startIndex = articleTopicQueryStartIndex) - 1
        "#" + this.substring(startIndex = articleTopicQueryPrefix.length, endIndex = articleTopicEndIndex)
    } else {
        null
    }
}

fun String.extractAdvancedSearchQuery(): String? {
    val queryField = "\"query\":\""
    val queryFieldStartIndex = this.indexOf(queryField)

    return if (queryFieldStartIndex != -1) {
        val queryStartIndex = queryFieldStartIndex + queryField.length
        val queryEndIndex = this.indexOf("\"", startIndex = queryStartIndex)
        this.substring(startIndex = queryStartIndex, endIndex = queryEndIndex)
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

package net.primal.domain

import net.primal.domain.common.cryptography.hexToNpubHrp

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

fun String.extractPubkeyFromFeedSpec(prefix: String? = null, suffix: String? = null): String? {
    val beforeProfileId = if (prefix != null) "$prefix,\"pubkey\":\"" else "\"pubkey\":\""
    val afterProfileId = if (suffix != null) "\"$suffix" else "\""
    return runCatching {
        this.substring(
            startIndex = beforeProfileId.length,
            endIndex = this.length - afterProfileId.length,
        )
    }.getOrNull()
}

fun String.isPubkeyFeedSpec(prefix: String? = null, suffix: String? = null): Boolean {
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

// fun String.isReadsBookmarkFeedSpec(): Boolean {
//    return isPubkeyFeedSpec(
//        prefix = "{\"id\":\"feed\",\"kind\":\"notes\",\"kinds\":[30023],\"notes\":\"bookmarks\"",
//        suffix = "}",
//    )
// }

fun buildLatestNotesUserFeedSpec(userId: String) = """{"id":"feed","kind":"notes","pubkey":"$userId"}"""

fun String.resolveFeedSpecKind(): FeedSpecKind? {
    return when {
        this.isNotesFeedSpec() -> FeedSpecKind.Notes
        this.isReadsFeedSpec() -> FeedSpecKind.Reads
        this.isImageSpec() -> FeedSpecKind.Notes
        this.isVideoSpec() -> FeedSpecKind.Notes
        this.isAudioSpec() -> FeedSpecKind.Notes
        else -> null
    }
}

fun String.isNotesFeedSpec() = this.contains("\"kind\":\"notes\"") || this.contains("kind:1")

fun String.isImageSpec() = this.contains("\"query\":\"filter:image")

fun String.isVideoSpec() = this.contains("\"query\":\"filter:video")

fun String.isAudioSpec() = this.contains("\"query\":\"filter:audio")

fun String.isReadsFeedSpec() = this.contains("\"kind\":\"reads\"") || this.contains("kind:30023")

fun String?.buildAdvancedSearchFeedSpec() = """{"id":"advsearch","query":"$this"}"""

fun buildAdvancedSearchNotesFeedSpec(query: String) = """{"id":"advsearch","query":"kind:1 $query"}"""

fun buildAdvancedSearchReadsFeedSpec(query: String) = """{"id":"advsearch","query":"kind:30023 $query"}"""

fun buildAdvancedSearchNotificationsFeedSpec(query: String) =
    """{"id":"advsearch","query":"kind:1 scope:mynotifications $query"}"""

fun buildReadsTopicFeedSpec(hashtag: String) = """{"kind":"reads","topic":"${hashtag.substring(startIndex = 1)}"}"""

const val exploreMediaFeedSpec = """{"id":"explore-media"}"""

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

fun String.isPremiumFeedSpec() = this.contains("pas:1")

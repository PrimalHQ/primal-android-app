package net.primal.android.settings.repository

import net.primal.android.core.utils.isValidNostrPublicKey
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.feed.db.Feed
import net.primal.android.nostr.ext.isNPub
import net.primal.android.nostr.model.primal.content.ContentFeedData

const val LWR_DIRECTIVE_PREFIX = "withreplies"

fun String.toLatestWithRepliesDirective() = "$LWR_DIRECTIVE_PREFIX;$this"

fun Feed.isLatest(userId: String) = this.directive == userId

fun Feed.isLatestWithReplies(userId: String) = this.directive == userId.toLatestWithRepliesDirective()

fun String.isLatestWithRepliesDirective(): Boolean {
    return startsWith(LWR_DIRECTIVE_PREFIX) && split(";").last().hexToNpubHrp().isNPub()
}

fun ContentFeedData.asFeedPO(): Feed? {
    return when {
        includeReplies == false || includeReplies == null -> Feed(name = name, directive = directive)
        directive.isValidNostrPublicKey() -> {
            Feed(name = name, directive = directive.toLatestWithRepliesDirective())
        }
        else -> null
    }
}

fun Feed.asContentFeedData(): ContentFeedData {
    return when {
        directive.isLatestWithRepliesDirective() -> ContentFeedData(
            name = name,
            directive = directive.split(";").last(),
            includeReplies = true,
        )

        else -> ContentFeedData(name = name, directive = directive)
    }
}

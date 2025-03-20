package net.primal.android.feeds

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.crypto.hexToNpubHrp
import net.primal.domain.extractAdvancedSearchQuery
import net.primal.domain.extractPubkeyFromFeedSpec
import net.primal.domain.extractSimpleSearchQuery
import net.primal.domain.extractTopicFromFeedSpec
import net.primal.domain.isAdvancedSearchFeedSpec
import net.primal.domain.isNotesFeedSpec
import net.primal.domain.isPubkeyFeedSpec
import net.primal.domain.isReadsFeedSpec
import net.primal.domain.isSearchFeedSpec
import net.primal.domain.isSimpleSearchFeedSpec

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

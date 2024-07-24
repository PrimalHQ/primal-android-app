package net.primal.android.nostr.ext

import kotlinx.serialization.encodeToString
import net.primal.android.articles.db.ArticleData
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.attachments.domain.CdnResource
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.toJsonObject
import net.primal.android.core.utils.parseHashtags
import net.primal.android.core.utils.parseUris
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.PrimalEvent
import timber.log.Timber

fun List<PrimalEvent>.mapNotNullReferencedEventsAsArticleDataPO(
    wordsCountMap: Map<String, Int>,
    cdnResources: Map<String, CdnResource>,
) = this.mapNotNull { it.takeContentOrNull<NostrEvent>() }
    .filter { event -> event.kind == NostrEventKind.LongFormContent.value }
    .mapNotNull { event ->
        event.asArticleData(
            wordsCount = wordsCountMap[event.id],
            cdnResources = cdnResources,
        )
    }

fun List<NostrEvent>.mapNotNullAsArticleDataPO(
    wordsCountMap: Map<String, Int>,
    cdnResources: Map<String, CdnResource>,
) = this.mapNotNull { event ->
    event.asArticleData(
        wordsCount = wordsCountMap[event.id],
        cdnResources = cdnResources,
    )
}

fun NostrEvent.asArticleData(wordsCount: Int?, cdnResources: Map<String, CdnResource>): ArticleData? {
    val identifier = tags.findFirstIdentifier()
    val title = tags.findFirstTitle()
    val raw = NostrJson.encodeToString(this.toJsonObject())

    if (identifier == null || title == null) {
        Timber.w("Unable to parse long form content: $raw")
        return null
    }

    return ArticleData(
        eventId = this.id,
        articleId = identifier,
        authorId = this.pubKey,
        title = title,
        createdAt = this.createdAt,
        publishedAt = tags.findFirstPublishedAt()?.toLongOrNull() ?: this.createdAt,
        content = this.content,
        uris = this.content.parseUris(),
        hashtags = this.parseHashtags(),
        raw = raw,
        imageCdnImage = tags.findFirstImage()?.let { imageUrl ->
            CdnImage(
                sourceUrl = imageUrl,
                variants = cdnResources[imageUrl]?.variants ?: emptyList(),
            )
        },
        summary = tags.findFirstSummary(),
        wordsCount = wordsCount,
    )
}

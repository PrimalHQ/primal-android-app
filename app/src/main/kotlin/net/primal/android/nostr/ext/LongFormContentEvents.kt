package net.primal.android.nostr.ext

import kotlinx.serialization.encodeToString
import net.primal.android.articles.db.ArticleData
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.attachments.domain.CdnResource
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.toJsonObject
import net.primal.android.core.utils.parseHashtags
import net.primal.android.core.utils.parseHashtagsFromNostrEventTags
import net.primal.android.core.utils.parseUris
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.PrimalEvent
import timber.log.Timber

fun List<PrimalEvent>.mapNotNullReferencedEventsAsArticleDataPO(
    wordsCountMap: Map<String, Int>,
    cdnResources: Map<String, CdnResource>,
): List<ArticleData> {
    val mappedFromNostrEvents = this.mapNotNull { it.takeContentOrNull<NostrEvent>() }
        .filter { event -> event.kind == NostrEventKind.LongFormContent.value }
        .mapNotNull { event ->
            event.asArticleData(
                wordsCount = wordsCountMap[event.id],
                cdnResources = cdnResources,
            )
        }

    val mappedFromPrimalEvents = this.mapNotNull { it.takeContentOrNull<PrimalEvent>() }
        .filter { event -> event.kind == NostrEventKind.PrimalLongFormContent.value }
        .mapNotNull { event ->
            event.asArticleData(
                wordsCount = wordsCountMap[event.id],
                cdnResources = cdnResources,
            )
        }

    return mappedFromNostrEvents + mappedFromPrimalEvents
}

fun List<NostrEvent>.mapNotNullPrimalEventAsArticleDataPO(
    wordsCountMap: Map<String, Int> = emptyMap(),
    cdnResources: Map<String, CdnResource> = emptyMap(),
) = this.mapNotNull { nostrEvent ->
    nostrEvent.asArticleData(
        wordsCount = wordsCountMap[nostrEvent.id],
        cdnResources = cdnResources,
    )
}

fun List<NostrEvent>.mapNotNullAsArticleDataPO(
    wordsCountMap: Map<String, Int> = emptyMap(),
    cdnResources: Map<String, CdnResource> = emptyMap(),
) = this.mapNotNull { event ->
    event.asArticleData(
        wordsCount = wordsCountMap[event.id],
        cdnResources = cdnResources,
    )
}

private fun NostrEvent.asArticleData(wordsCount: Int?, cdnResources: Map<String, CdnResource>): ArticleData? {
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

private fun PrimalEvent.asArticleData(wordsCount: Int?, cdnResources: Map<String, CdnResource>): ArticleData? {
    val identifier = tags.findFirstIdentifier()
    val title = tags.findFirstTitle()
    val authorId = this.pubKey
    val eventId = this.id
    val createdAt = this.createdAt

    if (eventId == null || identifier == null || title == null || authorId == null || createdAt == null) {
        Timber.w("Unable to parse long form content: $this")
        return null
    }

    return ArticleData(
        eventId = eventId,
        articleId = identifier,
        authorId = authorId,
        title = title,
        createdAt = createdAt,
        publishedAt = tags.findFirstPublishedAt()?.toLongOrNull() ?: createdAt,
        content = "",
        uris = emptyList(),
        hashtags = tags.parseHashtagsFromNostrEventTags().toList(),
        raw = null,
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

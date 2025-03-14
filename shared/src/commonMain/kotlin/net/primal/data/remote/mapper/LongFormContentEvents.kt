package net.primal.data.remote.mapper

import io.github.aakira.napier.Napier
import net.primal.data.local.dao.reads.ArticleData
import net.primal.data.serialization.NostrJson
import net.primal.data.serialization.toJsonObject
import net.primal.domain.CdnImage
import net.primal.domain.CdnResource
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.findFirstIdentifier
import net.primal.domain.nostr.findFirstImage
import net.primal.domain.nostr.findFirstPublishedAt
import net.primal.domain.nostr.findFirstSummary
import net.primal.domain.nostr.findFirstTitle
import net.primal.domain.nostr.utils.parseHashtags
import net.primal.domain.PrimalEvent


fun List<NostrEvent>.mapNotNullAsArticleDataPO(
    wordsCountMap: Map<String, Int> = emptyMap(),
    cdnResources: Map<String, CdnResource> = emptyMap(),
) = this.mapNotNull { event ->
    event.asArticleData(
        wordsCount = wordsCountMap[event.id],
        cdnResources = cdnResources,
    )
}

fun List<PrimalEvent>.mapReferencedEventsAsArticleDataPO(
    wordsCountMap: Map<String, Int> = emptyMap(),
    cdnResources: Map<String, CdnResource> = emptyMap(),
) = this.mapNotNull { it.takeContentOrNull<NostrEvent>() }
    .filter { event -> event.kind == NostrEventKind.LongFormContent.value }
    .mapNotNull { event -> event.asArticleData(wordsCount = wordsCountMap[event.id], cdnResources = cdnResources) }

private fun NostrEvent.asArticleData(wordsCount: Int?, cdnResources: Map<String, CdnResource>): ArticleData? {
    val identifier = tags.findFirstIdentifier()
    val title = tags.findFirstTitle()
    val raw = NostrJson.encodeToString(this.toJsonObject())

    if (identifier == null || title == null) {
        Napier.w("Unable to parse long form content: $raw")
        return null
    }

    return ArticleData(
        aTag = "${NostrEventKind.LongFormContent.value}:${this.pubKey}:$identifier",
        eventId = this.id,
        articleId = identifier,
        authorId = this.pubKey,
        title = title,
        createdAt = this.createdAt,
        publishedAt = tags.findFirstPublishedAt()?.toLongOrNull() ?: this.createdAt,
        content = this.content,
        // TODO Rewire uri parsing once is implemented for KMP
        uris = emptyList(),
//        uris = this.content.parseUris(),
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

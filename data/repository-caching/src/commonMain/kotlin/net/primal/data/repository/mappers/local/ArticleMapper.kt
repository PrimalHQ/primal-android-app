package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.reads.Article as ArticlePO
import net.primal.domain.model.Article as ArticleDO

fun ArticlePO.asArticleDO(): ArticleDO {
    return ArticleDO(
        aTag = this.data.aTag,
        eventId = this.data.eventId,
        articleId = this.data.articleId,
        authorId = this.data.authorId,
        createdAt = this.data.createdAt,
        content = this.data.content,
        title = this.data.title,
        publishedAt = this.data.publishedAt,
        articleRawJson = this.data.raw,
        imageCdnImage = this.data.imageCdnImage,
        summary = this.data.summary,
        authorMetadataId = this.data.authorMetadataId,
        wordsCount = this.data.wordsCount,
        uris = this.data.uris,
        hashtags = this.data.hashtags,
        author = this.author?.asProfileDataDO(),
        eventStats = this.eventStats?.asNostrEventStats(),
        userEventStats = this.userEventStats?.asNostrEventUserStats(),
        eventZaps = this.eventZaps.map { it.asEventZapDO() },
        bookmark = this.bookmark?.asPublicBookmark(),
        highlights = this.highlights.map { it.asHighlightDO() },
    )
}

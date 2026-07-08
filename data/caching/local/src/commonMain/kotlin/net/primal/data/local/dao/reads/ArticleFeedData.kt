package net.primal.data.local.dao.reads

import net.primal.domain.links.CdnImage

/**
 * Column-subset of [ArticleData] for the article feed projection — excludes the fat
 * `content` and `raw` columns (full article markdown and raw event JSON) that feed
 * cards never render, plus `uris`, `hashtags` and `authorMetadataId` which the feed
 * does not read.
 */
data class ArticleFeedData(
    val aTag: String,
    val eventId: String,
    val articleId: String,
    val authorId: String,
    val createdAt: Long,
    val title: String,
    val publishedAt: Long,
    val imageCdnImage: CdnImage? = null,
    val summary: String? = null,
    val wordsCount: Int? = null,
    val client: String? = null,
)

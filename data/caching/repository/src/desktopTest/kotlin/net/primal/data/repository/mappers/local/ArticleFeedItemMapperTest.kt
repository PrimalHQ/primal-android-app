package net.primal.data.repository.mappers.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import net.primal.data.local.dao.bookmarks.PublicBookmark
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.dao.notes.FeedAuthorLite
import net.primal.data.local.dao.reads.ArticleFeedData
import net.primal.data.local.dao.reads.ArticleFeedItem
import net.primal.domain.bookmarks.BookmarkType
import net.primal.domain.events.ZapKind
import net.primal.domain.links.CdnImage
import net.primal.domain.membership.PrimalPremiumInfo

class ArticleFeedItemMapperTest {

    @Test
    fun asArticleDO_mapsArticleFeedData() {
        val article = articleFeedItem().asArticleDO()

        assertEquals("30023:author:article-1", article.aTag)
        assertEquals("event-1", article.eventId)
        assertEquals("article-1", article.articleId)
        assertEquals("author", article.authorId)
        assertEquals(1_700_000_000L, article.createdAt)
        assertEquals("title", article.title)
        assertEquals(1_700_000_100L, article.publishedAt)
        assertEquals(CdnImage(sourceUrl = "https://img"), article.imageCdnImage)
        assertEquals("summary", article.summary)
        assertEquals(420, article.wordsCount)
        assertEquals("client", article.client)
    }

    @Test
    fun asArticleDO_leavesOutFatColumnsAndHighlights() {
        val article = articleFeedItem().asArticleDO()

        assertEquals("", article.content)
        assertEquals("", article.articleRawJson)
        assertTrue(article.uris.isEmpty())
        assertTrue(article.hashtags.isEmpty())
        assertTrue(article.highlights.isEmpty())
        assertNull(article.authorMetadataId)
    }

    @Test
    fun asArticleDO_mapsLiteAuthor() {
        val article = articleFeedItem().asArticleDO()

        val author = assertNotNull(article.author)
        assertEquals("author", author.profileId)
        assertEquals("display name", author.displayName)
        assertEquals("handle", author.handle)
        assertEquals("nip05@primal.net", author.internetIdentifier)
        assertEquals(CdnImage(sourceUrl = "https://avatar"), author.avatarCdnImage)
        assertEquals("primal-name", author.primalName)
        assertEquals("primal-name", author.primalPremiumInfo?.primalName)
        assertEquals(listOf("https://blossom"), author.blossoms)
    }

    @Test
    fun asArticleDO_mapsStatsBookmarkAndGenericZapsOnly() {
        val article = articleFeedItem().asArticleDO()

        assertEquals(42, article.eventStats?.likes)
        assertEquals(true, article.userEventStats?.liked)
        assertNotNull(article.bookmark)
        assertEquals(1, article.eventZaps.size)
        assertEquals(ZAPPER_ID, article.eventZaps.first().zapperId)
    }

    private fun articleFeedItem() =
        ArticleFeedItem(
            data = ArticleFeedData(
                aTag = "30023:author:article-1",
                eventId = "event-1",
                articleId = "article-1",
                authorId = "author",
                createdAt = 1_700_000_000L,
                title = "title",
                publishedAt = 1_700_000_100L,
                imageCdnImage = CdnImage(sourceUrl = "https://img"),
                summary = "summary",
                wordsCount = 420,
                client = "client",
            ),
            author = FeedAuthorLite(
                ownerId = "author",
                displayName = "display name",
                handle = "handle",
                internetIdentifier = "nip05@primal.net",
                avatarCdnImage = CdnImage(sourceUrl = "https://avatar"),
                primalPremiumInfo = PrimalPremiumInfo(primalName = "primal-name"),
                blossoms = listOf("https://blossom"),
            ),
            eventStats = EventStats(eventId = "event-1", likes = 42),
            userEventStats = EventUserStats(eventId = "event-1", userId = "user", liked = true),
            eventZaps = listOf(
                eventZap(zapSenderId = ZAPPER_ID, zapKind = ZapKind.GENERIC),
                eventZap(zapSenderId = VOTER_ID, zapKind = ZapKind.VOTE),
            ),
            bookmark = PublicBookmark(
                tagValue = "30023:author:article-1",
                tagType = "a",
                bookmarkType = BookmarkType.Article,
                ownerId = "user",
            ),
        )

    private fun eventZap(zapSenderId: String, zapKind: ZapKind) =
        EventZap(
            eventId = "30023:author:article-1",
            zapSenderId = zapSenderId,
            zapReceiverId = "author",
            zapRequestAt = 1_700_000_000L,
            zapReceiptAt = 1_700_000_000L,
            amountInBtc = 0.0001,
            message = null,
            invoice = null,
            zapKind = zapKind,
        )

    private companion object {
        val ZAPPER_ID = "aa".repeat(32)
        val VOTER_ID = "bb".repeat(32)
    }
}

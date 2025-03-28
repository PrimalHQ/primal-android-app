package net.primal.data.repository.articles.processors

import net.primal.core.utils.asMapByKey
import net.primal.data.local.dao.events.eventRelayHintsUpserter
import net.primal.data.local.dao.threads.ArticleCommentCrossRef
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
import net.primal.data.remote.api.articles.ArticleResponse
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.flatMapNotNullAsLinkPreviewResource
import net.primal.data.remote.mapper.flatMapNotNullAsVideoThumbnailsMap
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.mappers.remote.flatMapArticlesAsEventUriPO
import net.primal.data.repository.mappers.remote.flatMapAsEventHintsPO
import net.primal.data.repository.mappers.remote.flatMapAsWordCount
import net.primal.data.repository.mappers.remote.mapAsEventZapDO
import net.primal.data.repository.mappers.remote.mapAsPostDataPO
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.mapNotNullAsArticleDataPO
import net.primal.data.repository.mappers.remote.mapNotNullAsEventStatsPO
import net.primal.data.repository.mappers.remote.mapNotNullAsEventUserStatsPO
import net.primal.data.repository.mappers.remote.mapNotNullAsPostDataPO
import net.primal.data.repository.mappers.remote.mapReferencedEventsAsHighlightDataPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames

suspend fun ArticleResponse.persistToDatabaseAsTransaction(userId: String, database: PrimalDatabase) {
    val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource()
    val eventHints = this.primalRelayHints.flatMapAsEventHintsPO()
    val wordsCountMap = this.primalLongFormWords.flatMapAsWordCount()

    val primalUserNames = this.primalUserNames.parseAndMapPrimalUserNames()
    val primalPremiumInfo = this.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
    val primalLegendProfiles = this.primalLegendProfiles.parseAndMapPrimalLegendProfiles()

    val blossomServers = this.blossomServers.mapAsMapPubkeyToListOfBlossomServers()

    val profiles = this.metadata.mapAsProfileDataPO(
        cdnResources = cdnResources,
        primalUserNames = primalUserNames,
        primalPremiumInfo = primalPremiumInfo,
        primalLegendProfiles = primalLegendProfiles,
        blossomServers = blossomServers,
    )
    val allArticles = this.articles.mapNotNullAsArticleDataPO(
        wordsCountMap = wordsCountMap,
        cdnResources = cdnResources,
    )
    val referencedHighlights = this.referencedEvents.mapReferencedEventsAsHighlightDataPO()
    val referencedPostsWithoutReplyTo = referencedEvents.mapNotNullAsPostDataPO()
    val referencedPostsWithReplyTo = referencedEvents.mapNotNullAsPostDataPO(
        referencedPosts = referencedPostsWithoutReplyTo,
        referencedArticles = allArticles,
        referencedHighlights = referencedHighlights,
    )

    val allNotes = this.notes.mapAsPostDataPO(
        referencedPosts = referencedPostsWithReplyTo,
        referencedArticles = allArticles,
        referencedHighlights = referencedHighlights,
    )

    val linkPreviews = primalLinkPreviews.flatMapNotNullAsLinkPreviewResource().asMapByKey { it.url }
    val videoThumbnails = this.cdnResources.flatMapNotNullAsVideoThumbnailsMap()

    val noteAttachments = allArticles.flatMapArticlesAsEventUriPO(
        cdnResources = cdnResources,
        linkPreviews = linkPreviews,
        videoThumbnails = videoThumbnails,
    )

    val eventZaps = this.zaps.mapAsEventZapDO(profilesMap = profiles.associateBy { it.ownerId })
    val eventStats = this.primalEventStats.mapNotNullAsEventStatsPO()
    val eventUserStats = this.primalEventUserStats.mapNotNullAsEventUserStatsPO(userId = userId)

    database.withTransaction {
        database.profiles().insertOrUpdateAll(data = profiles)
        database.posts().upsertAll(data = allNotes + referencedPostsWithReplyTo)
        database.articles().upsertAll(list = allArticles)
        database.eventStats().upsertAll(data = eventStats)
        database.eventUserStats().upsertAll(data = eventUserStats)
        database.eventZaps().upsertAll(data = eventZaps)
        database.highlights().upsertAll(data = referencedHighlights)
        database.eventUris().upsertAllEventUris(data = noteAttachments)

        val eventHintsDao = database.eventHints()
        val hintsMap = eventHints.associateBy { it.eventId }
        eventRelayHintsUpserter(dao = eventHintsDao, eventIds = eventHints.map { it.eventId }) {
            copy(relays = hintsMap[this.eventId]?.relays ?: emptyList())
        }
    }
}

suspend fun ArticleResponse.persistArticleCommentsToDatabase(
    articleId: String,
    articleAuthorId: String,
    database: PrimalDatabase,
) {
    val referencedNotes = this.referencedEvents.mapNotNullAsPostDataPO()
    val comments = this.notes.mapAsPostDataPO(referencedPosts = referencedNotes, emptyList(), emptyList())

    database.withTransaction {
        database.threadConversations().connectArticleWithComment(
            data = comments.map {
                ArticleCommentCrossRef(
                    articleId = articleId,
                    articleAuthorId = articleAuthorId,
                    commentNoteId = it.postId,
                )
            },
        )
    }
}

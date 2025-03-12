package net.primal.repository.processors

import net.primal.api.feed.model.FeedResponse
import net.primal.core.utils.asMapByKey
import net.primal.db.PrimalDatabase
import net.primal.db.conversation.NoteConversationCrossRef
import net.primal.db.events.eventRelayHintsUpserter
import net.primal.db.withTransaction
import net.primal.networking.model.NostrEvent
import net.primal.repository.processors.mappers.flatMapAsEventHintsPO
import net.primal.repository.processors.mappers.flatMapNotNullAsCdnResource
import net.primal.repository.processors.mappers.flatMapNotNullAsLinkPreviewResource
import net.primal.repository.processors.mappers.flatMapNotNullAsVideoThumbnailsMap
import net.primal.repository.processors.mappers.flatMapPostsAsEventUriPO
import net.primal.repository.processors.mappers.flatMapPostsAsNoteNostrUriPO
import net.primal.repository.processors.mappers.mapAsEventZapDO
import net.primal.repository.processors.mappers.mapAsMapPubkeyToListOfBlossomServers
import net.primal.repository.processors.mappers.mapAsPostDataPO
import net.primal.repository.processors.mappers.mapAsProfileDataPO
import net.primal.repository.processors.mappers.mapNotNullAsEventStatsPO
import net.primal.repository.processors.mappers.mapNotNullAsEventUserStatsPO
import net.primal.repository.processors.mappers.mapNotNullAsPostDataPO
import net.primal.repository.processors.mappers.parseAndMapPrimalLegendProfiles
import net.primal.repository.processors.mappers.parseAndMapPrimalPremiumInfo
import net.primal.repository.processors.mappers.parseAndMapPrimalUserNames
import net.primal.serialization.json.NostrJson
import net.primal.serialization.json.decodeFromStringOrNull

// TODO Add support for articles, highlights and reposts once ported

suspend fun FeedResponse.persistToDatabaseAsTransaction(userId: String, database: PrimalDatabase) {
    val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
    val videoThumbnails = this.cdnResources.flatMapNotNullAsVideoThumbnailsMap()
    val linkPreviews = primalLinkPreviews.flatMapNotNullAsLinkPreviewResource().asMapByKey { it.url }
    val eventHints = this.primalRelayHints.flatMapAsEventHintsPO()

//    val articles = this.articles.mapNotNullAsArticleDataPO(cdnResources = cdnResources)
//    val referencedArticles = this.referencedEvents.mapReferencedEventsAsArticleDataPO(cdnResources = cdnResources)
//    val referencedHighlights = this.referencedEvents.mapReferencedEventsAsHighlightDataPO()
//    val allArticles = articles + referencedArticles

    val referencedPostsWithoutReplyTo = referencedEvents.mapNotNullAsPostDataPO()
    val referencedPostsWithReplyTo = referencedEvents.mapNotNullAsPostDataPO(
        referencedPosts = referencedPostsWithoutReplyTo,
//        referencedArticles = allArticles,
//        referencedHighlights = referencedHighlights,
    )
    val feedPosts = notes.mapAsPostDataPO(
        referencedPosts = referencedPostsWithReplyTo,
//        referencedArticles = allArticles,
//        referencedHighlights = referencedHighlights,
    )

    val primalUserNames = this.primalUserNames.parseAndMapPrimalUserNames()
    val primalPremiumInfo = this.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
    val primalLegendProfiles = this.primalLegendProfiles.parseAndMapPrimalLegendProfiles()

    val blossomServers = this.blossomServers.mapAsMapPubkeyToListOfBlossomServers()

    val profiles = metadata.mapAsProfileDataPO(
        cdnResources = cdnResources,
        primalUserNames = primalUserNames,
        primalPremiumInfo = primalPremiumInfo,
        primalLegendProfiles = primalLegendProfiles,
        blossomServers = blossomServers,
    )
    val profileIdToProfileDataMap = profiles.asMapByKey { it.ownerId }

    val allPosts = (referencedPostsWithReplyTo + feedPosts).map { postData ->
        val eventIdMap = profileIdToProfileDataMap.mapValues { it.value.eventId }
        postData.copy(authorMetadataId = eventIdMap[postData.authorId])
    }

    val noteAttachments = allPosts.flatMapPostsAsEventUriPO(
        cdnResources = cdnResources,
        linkPreviews = linkPreviews,
        videoThumbnails = videoThumbnails,
    )

    val refEvents = referencedEvents.mapNotNull { NostrJson.decodeFromStringOrNull<NostrEvent>(it.content) }

    val noteNostrUris = allPosts.flatMapPostsAsNoteNostrUriPO(
        eventIdToNostrEvent = refEvents.associateBy { it.id },
        postIdToPostDataMap = allPosts.groupBy { it.postId }.mapValues { it.value.first() },
//        articleIdToArticle = allArticles.groupBy { it.articleId }.mapValues { it.value.first() },
        profileIdToProfileDataMap = profileIdToProfileDataMap,
        cdnResources = cdnResources,
        videoThumbnails = videoThumbnails,
        linkPreviews = linkPreviews,
    )

    val eventZaps = zaps.mapAsEventZapDO(profilesMap = profiles.associateBy { it.ownerId })
//    val reposts = reposts.mapNotNullAsRepostDataPO()
    val postStats = primalEventStats.mapNotNullAsEventStatsPO()
    val userPostStats = primalEventUserStats.mapNotNullAsEventUserStatsPO(userId = userId)

    database.withTransaction {
        database.profiles().insertOrUpdateAll(data = profiles)
        database.posts().upsertAll(data = allPosts)
        database.eventUris().upsertAllEventUris(data = noteAttachments)
        database.eventUris().upsertAllEventNostrUris(data = noteNostrUris)
//        database.reposts().upsertAll(data = reposts)
        database.eventZaps().upsertAll(data = eventZaps)
        database.eventStats().upsertAll(data = postStats)
        database.eventUserStats().upsertAll(data = userPostStats)
//        database.articles().upsertAll(list = allArticles)
//        database.highlights().upsertAll(data = referencedHighlights)

        val eventHintsDao = database.eventHints()
        val hintsMap = eventHints.associateBy { it.eventId }
        eventRelayHintsUpserter(dao = eventHintsDao, eventIds = eventHints.map { it.eventId }) {
            copy(relays = hintsMap[this.eventId]?.relays ?: emptyList())
        }
    }
}

suspend fun FeedResponse.persistNoteRepliesAndArticleCommentsToDatabase(noteId: String, database: PrimalDatabase) {
    val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
//    val articles = this.articles.mapNotNullAsArticleDataPO(cdnResources = cdnResources)

    database.withTransaction {
        database.threadConversations().connectNoteWithReply(
            data = notes.map {
                NoteConversationCrossRef(
                    noteId = noteId,
                    replyNoteId = it.id,
                )
            },
        )
//        database.threadConversations().connectArticleWithComment(
//            data = articles.map { article ->
//                ArticleCommentCrossRef(
//                    articleId = article.articleId,
//                    articleAuthorId = article.authorId,
//                    commentNoteId = noteId,
//                )
//            },
//        )
    }
}

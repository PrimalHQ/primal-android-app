package net.primal.data.remote.processors

import net.primal.core.utils.asMapByKey
import net.primal.data.local.dao.events.eventRelayHintsUpserter
import net.primal.data.local.dao.threads.ArticleCommentCrossRef
import net.primal.data.local.dao.threads.NoteConversationCrossRef
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
import net.primal.data.remote.api.feed.model.FeedResponse
import net.primal.data.remote.mapper.flatMapAsEventHintsPO
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.flatMapNotNullAsLinkPreviewResource
import net.primal.data.remote.mapper.flatMapNotNullAsVideoThumbnailsMap
import net.primal.data.remote.mapper.flatMapPostsAsEventUriPO
import net.primal.data.remote.mapper.flatMapPostsAsReferencedNostrUriDO
import net.primal.data.remote.mapper.mapAsEventZapDO
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.remote.mapper.mapAsPostDataPO
import net.primal.data.remote.mapper.mapAsProfileDataPO
import net.primal.data.remote.mapper.mapNotNullAsArticleDataPO
import net.primal.data.remote.mapper.mapNotNullAsEventStatsPO
import net.primal.data.remote.mapper.mapNotNullAsEventUserStatsPO
import net.primal.data.remote.mapper.mapNotNullAsPostDataPO
import net.primal.data.remote.mapper.mapNotNullAsRepostDataPO
import net.primal.data.remote.mapper.mapReferencedEventsAsArticleDataPO
import net.primal.data.remote.mapper.mapReferencedEventsAsHighlightDataPO
import net.primal.data.remote.mapper.mapReferencedNostrUriAsEventUriNostrPO
import net.primal.data.remote.mapper.parseAndMapPrimalLegendProfiles
import net.primal.data.remote.mapper.parseAndMapPrimalPremiumInfo
import net.primal.data.remote.mapper.parseAndMapPrimalUserNames
import net.primal.data.serialization.NostrJson
import net.primal.data.serialization.decodeFromStringOrNull
import net.primal.domain.nostr.NostrEvent

suspend fun FeedResponse.persistToDatabaseAsTransaction(userId: String, database: PrimalDatabase) {
    val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
    val videoThumbnails = this.cdnResources.flatMapNotNullAsVideoThumbnailsMap()
    val linkPreviews = primalLinkPreviews.flatMapNotNullAsLinkPreviewResource().asMapByKey { it.url }
    val eventHints = this.primalRelayHints.flatMapAsEventHintsPO()

    val articles = this.articles.mapNotNullAsArticleDataPO(cdnResources = cdnResources)
    val referencedArticles = this.referencedEvents.mapReferencedEventsAsArticleDataPO(cdnResources = cdnResources)
    val referencedHighlights = this.referencedEvents.mapReferencedEventsAsHighlightDataPO()
    val allArticles = articles + referencedArticles

    val referencedPostsWithoutReplyTo = referencedEvents.mapNotNullAsPostDataPO()
    val referencedPostsWithReplyTo = referencedEvents.mapNotNullAsPostDataPO(
        referencedPosts = referencedPostsWithoutReplyTo,
        referencedArticles = allArticles,
        referencedHighlights = referencedHighlights,
    )
    val feedPosts = notes.mapAsPostDataPO(
        referencedPosts = referencedPostsWithReplyTo,
        referencedArticles = allArticles,
        referencedHighlights = referencedHighlights,
    )

    val primalUserNames = this.primalUserNames.parseAndMapPrimalUserNames()
    val primalPremiumInfo = this.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
    val primalLegendProfiles = this.primalLegendProfiles.parseAndMapPrimalLegendProfiles()

    val blossomServers = this.blossomServers.mapAsMapPubkeyToListOfBlossomServers()

    val profiles = metadata.mapAsProfileDataPO(
        cdnResourcesMap = cdnResources,
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

    val noteNostrUris = allPosts.flatMapPostsAsReferencedNostrUriDO(
        eventIdToNostrEvent = refEvents.associateBy { it.id },
        postIdToPostDataMap = allPosts.groupBy { it.postId }.mapValues { it.value.first() },
        articleIdToArticle = allArticles.groupBy { it.articleId }.mapValues { it.value.first() },
        profileIdToProfileDataMap = profileIdToProfileDataMap,
        cdnResources = cdnResources,
        videoThumbnails = videoThumbnails,
        linkPreviews = linkPreviews,
    ).mapReferencedNostrUriAsEventUriNostrPO()

    val eventZaps = zaps.mapAsEventZapDO(profilesMap = profiles.associateBy { it.ownerId })
    val reposts = reposts.mapNotNullAsRepostDataPO()
    val postStats = primalEventStats.mapNotNullAsEventStatsPO()
    val userPostStats = primalEventUserStats.mapNotNullAsEventUserStatsPO(userId = userId)

    database.withTransaction {
        database.profiles().insertOrUpdateAll(data = profiles)
        database.posts().upsertAll(data = allPosts)
        database.eventUris().upsertAllEventUris(data = noteAttachments)
        database.eventUris().upsertAllEventNostrUris(data = noteNostrUris)
        database.reposts().upsertAll(data = reposts)
        database.eventZaps().upsertAll(data = eventZaps)
        database.eventStats().upsertAll(data = postStats)
        database.eventUserStats().upsertAll(data = userPostStats)
        database.articles().upsertAll(list = allArticles)
        database.highlights().upsertAll(data = referencedHighlights)

        val eventHintsDao = database.eventHints()
        val hintsMap = eventHints.associateBy { it.eventId }
        eventRelayHintsUpserter(dao = eventHintsDao, eventIds = eventHints.map { it.eventId }) {
            copy(relays = hintsMap[this.eventId]?.relays ?: emptyList())
        }
    }
}

suspend fun FeedResponse.persistNoteRepliesAndArticleCommentsToDatabase(noteId: String, database: PrimalDatabase) {
    val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
    val articles = this.articles.mapNotNullAsArticleDataPO(cdnResources = cdnResources)

    database.withTransaction {
        database.threadConversations().connectNoteWithReply(
            data = notes.map {
                NoteConversationCrossRef(
                    noteId = noteId,
                    replyNoteId = it.id,
                )
            },
        )
        database.threadConversations().connectArticleWithComment(
            data = articles.map { article ->
                ArticleCommentCrossRef(
                    articleId = article.articleId,
                    articleAuthorId = article.authorId,
                    commentNoteId = noteId,
                )
            },
        )
    }
}

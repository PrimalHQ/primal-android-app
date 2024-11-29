package net.primal.android.notes.repository

import androidx.room.withTransaction
import net.primal.android.attachments.ext.flatMapPostsAsNoteAttachmentPO
import net.primal.android.core.ext.asMapByKey
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.db.eventRelayHintsUpserter
import net.primal.android.nostr.ext.flatMapAsEventHintsPO
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.flatMapNotNullAsLinkPreviewResource
import net.primal.android.nostr.ext.flatMapNotNullAsVideoThumbnailsMap
import net.primal.android.nostr.ext.flatMapPostsAsNoteNostrUriPO
import net.primal.android.nostr.ext.mapAsEventZapDO
import net.primal.android.nostr.ext.mapAsPostDataPO
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.mapNotNullAsArticleDataPO
import net.primal.android.nostr.ext.mapNotNullAsEventStatsPO
import net.primal.android.nostr.ext.mapNotNullAsEventUserStatsPO
import net.primal.android.nostr.ext.mapNotNullAsPostDataPO
import net.primal.android.nostr.ext.mapNotNullAsRepostDataPO
import net.primal.android.nostr.ext.mapReferencedEventsAsArticleDataPO
import net.primal.android.nostr.ext.parseAndMapPrimalLegendProfiles
import net.primal.android.nostr.ext.parseAndMapPrimalUserNames
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.notes.api.model.FeedResponse
import net.primal.android.thread.db.ArticleCommentCrossRef
import net.primal.android.thread.db.NoteConversationCrossRef

suspend fun FeedResponse.persistToDatabaseAsTransaction(userId: String, database: PrimalDatabase) {
    val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
    val videoThumbnails = this.cdnResources.flatMapNotNullAsVideoThumbnailsMap()
    val linkPreviews = primalLinkPreviews.flatMapNotNullAsLinkPreviewResource().asMapByKey { it.url }
    val eventHints = this.primalRelayHints.flatMapAsEventHintsPO()

    val referencedPostsWithoutReplyTo = referencedEvents.mapNotNullAsPostDataPO()
    val referencedPostsWithReplyTo = referencedEvents.mapNotNullAsPostDataPO(
        referencedPosts = referencedPostsWithoutReplyTo,
    )
    val feedPosts = posts.mapAsPostDataPO(referencedPosts = referencedPostsWithReplyTo)

    val articles = this.articles.mapNotNullAsArticleDataPO(cdnResources = cdnResources)
    val referencedArticles = this.referencedEvents.mapReferencedEventsAsArticleDataPO(cdnResources = cdnResources)
    val allArticles = articles + referencedArticles

    val primalUserNames = this.primalUserNames.parseAndMapPrimalUserNames()
    val primalLegendProfiles = this.primalLegendProfiles.parseAndMapPrimalLegendProfiles()

    val profiles = metadata.mapAsProfileDataPO(
        cdnResources = cdnResources,
        primalUserNames = primalUserNames,
        primalLegendProfiles = primalLegendProfiles,
    )
    val profileIdToProfileDataMap = profiles.asMapByKey { it.ownerId }

    val allPosts = (referencedPostsWithReplyTo + feedPosts).map { postData ->
        val eventIdMap = profileIdToProfileDataMap.mapValues { it.value.eventId }
        postData.copy(authorMetadataId = eventIdMap[postData.authorId])
    }

    val noteAttachments = allPosts.flatMapPostsAsNoteAttachmentPO(
        cdnResources = cdnResources,
        linkPreviews = linkPreviews,
        videoThumbnails = videoThumbnails,
    )

    val refEvents = referencedEvents.mapNotNull { NostrJson.decodeFromStringOrNull<NostrEvent>(it.content) }

    val noteNostrUris = allPosts.flatMapPostsAsNoteNostrUriPO(
        eventIdToNostrEvent = refEvents.associateBy { it.id },
        postIdToPostDataMap = allPosts.groupBy { it.postId }.mapValues { it.value.first() },
        articleIdToArticle = allArticles.groupBy { it.articleId }.mapValues { it.value.first() },
        profileIdToProfileDataMap = profileIdToProfileDataMap,
        cdnResources = cdnResources,
        videoThumbnails = videoThumbnails,
        linkPreviews = linkPreviews,
    )

    val eventZaps = zaps.mapAsEventZapDO(profilesMap = profiles.associateBy { it.ownerId })
    val reposts = reposts.mapNotNullAsRepostDataPO()
    val postStats = primalEventStats.mapNotNullAsEventStatsPO()
    val userPostStats = primalEventUserStats.mapNotNullAsEventUserStatsPO(userId = userId)

    database.withTransaction {
        database.profiles().upsertAll(data = profiles)
        database.posts().upsertAll(data = allPosts)
        database.attachments().upsertAllNoteAttachments(data = noteAttachments)
        database.attachments().upsertAllNostrUris(data = noteNostrUris)
        database.reposts().upsertAll(data = reposts)
        database.eventZaps().upsertAll(data = eventZaps)
        database.eventStats().upsertAll(data = postStats)
        database.eventUserStats().upsertAll(data = userPostStats)
        database.articles().upsertAll(list = allArticles)

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
            data = posts.map {
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

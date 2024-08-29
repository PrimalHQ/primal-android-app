package net.primal.android.feed.repository

import androidx.room.withTransaction
import net.primal.android.attachments.ext.flatMapPostsAsNoteAttachmentPO
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.model.FeedResponse
import net.primal.android.nostr.db.eventHintsUpserter
import net.primal.android.nostr.ext.flatMapAsEventHintsPO
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.flatMapNotNullAsLinkPreviewResource
import net.primal.android.nostr.ext.flatMapNotNullAsVideoThumbnailsMap
import net.primal.android.nostr.ext.flatMapPostsAsNoteNostrUriPO
import net.primal.android.nostr.ext.mapAsPostDataPO
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.mapNotNullAsEventStatsPO
import net.primal.android.nostr.ext.mapNotNullAsEventUserStatsPO
import net.primal.android.nostr.ext.mapNotNullAsPostDataPO
import net.primal.android.nostr.ext.mapNotNullAsRepostDataPO
import net.primal.android.thread.db.NoteConversationCrossRef

suspend fun FeedResponse.persistToDatabaseAsTransaction(userId: String, database: PrimalDatabase) {
    val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
    val videoThumbnails = this.cdnResources.flatMapNotNullAsVideoThumbnailsMap()
    val linkPreviews = primalLinkPreviews.flatMapNotNullAsLinkPreviewResource().asMapByKey { it.url }
    val eventHints = this.primalRelayHints.flatMapAsEventHintsPO()

    val referencedPostsWithoutReplyTo = referencedPosts.mapNotNullAsPostDataPO()
    val referencedPostsWithReplyTo = referencedPosts.mapNotNullAsPostDataPO(
        referencedPosts = referencedPostsWithoutReplyTo,
    )
    val feedPosts = posts.mapAsPostDataPO(referencedPosts = referencedPostsWithReplyTo)

    val profiles = metadata.mapAsProfileDataPO(cdnResources = cdnResources)
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

    val noteNostrUris = allPosts.flatMapPostsAsNoteNostrUriPO(
        postIdToPostDataMap = allPosts.groupBy { it.postId }.mapValues { it.value.first() },
        profileIdToProfileDataMap = profileIdToProfileDataMap,
    )

    val reposts = reposts.mapNotNullAsRepostDataPO()
    val postStats = primalEventStats.mapNotNullAsEventStatsPO()
    val userPostStats = primalEventUserStats.mapNotNullAsEventUserStatsPO(userId = userId)

    database.withTransaction {
        database.profiles().upsertAll(data = profiles)
        database.posts().upsertAll(data = allPosts)
        database.attachments().upsertAllNoteAttachments(data = noteAttachments)
        database.attachments().upsertAllNostrUris(data = noteNostrUris)
        database.reposts().upsertAll(data = reposts)
        database.eventStats().upsertAll(data = postStats)
        database.eventUserStats().upsertAll(data = userPostStats)

        val eventHintsDao = database.eventHints()
        val hintsMap = eventHints.associateBy { it.eventId }
        eventHintsUpserter(dao = eventHintsDao, eventIds = eventHints.map { it.eventId }) {
            copy(relays = hintsMap[this.eventId]?.relays ?: emptyList())
        }
    }
}

suspend fun FeedResponse.persistNoteRepliesAndArticleCommentsToDatabase(noteId: String, database: PrimalDatabase) {
    database.withTransaction {
        database.threadConversations().connectNoteWithReply(
            data = posts.map {
                NoteConversationCrossRef(
                    noteId = noteId,
                    replyNoteId = it.id,
                )
            },
        )
    }
}

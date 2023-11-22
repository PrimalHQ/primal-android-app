package net.primal.android.feed.repository

import androidx.room.withTransaction
import net.primal.android.attachments.ext.flatMapPostsAsNoteAttachmentPO
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.model.FeedResponse
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.flatMapNotNullAsLinkPreviewResource
import net.primal.android.nostr.ext.flatMapNotNullAsVideoThumbnailsMap
import net.primal.android.nostr.ext.flatMapPostsAsNoteNostrUriPO
import net.primal.android.nostr.ext.mapAsPostDataPO
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.mapNotNullAsPostDataPO
import net.primal.android.nostr.ext.mapNotNullAsPostStatsPO
import net.primal.android.nostr.ext.mapNotNullAsPostUserStatsPO
import net.primal.android.nostr.ext.mapNotNullAsRepostDataPO

suspend fun FeedResponse.persistToDatabaseAsTransaction(userId: String, database: PrimalDatabase) {
    val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
    val videoThumbnails = this.cdnResources.flatMapNotNullAsVideoThumbnailsMap()
    val linkPreviews = primalLinkPreviews.flatMapNotNullAsLinkPreviewResource().asMapByKey { it.url }

    val feedPosts = posts.mapAsPostDataPO()
    val referencedPosts = referencedPosts.mapNotNullAsPostDataPO()

    val profiles = metadata.mapAsProfileDataPO(cdnResources = cdnResources)
    val profileIdToProfileDataMap = profiles.asMapByKey { it.ownerId }

    val allPosts = (feedPosts + referencedPosts).map { postData ->
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
    val postStats = primalEventStats.mapNotNullAsPostStatsPO()
    val userPostStats = primalEventUserStats.mapNotNullAsPostUserStatsPO(userId = userId)

    database.withTransaction {
        database.profiles().upsertAll(data = profiles)
        database.posts().upsertAll(data = allPosts)
        database.attachments().upsertAllNoteAttachments(data = noteAttachments)
        database.attachments().upsertAllNostrUris(data = noteNostrUris)
        database.reposts().upsertAll(data = reposts)
        database.postStats().upsertAll(data = postStats)
        database.postUserStats().upsertAll(data = userPostStats)
    }
}

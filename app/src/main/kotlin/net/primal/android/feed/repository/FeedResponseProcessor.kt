package net.primal.android.feed.repository

import androidx.room.withTransaction
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.model.FeedResponse
import net.primal.android.nostr.ext.flatMapAsPostMediaResourcePO
import net.primal.android.nostr.ext.flatMapAsPostNostrResourcePO
import net.primal.android.nostr.ext.flatMapNotNullAsMediaResourcePO
import net.primal.android.nostr.ext.mapAsPostDataPO
import net.primal.android.nostr.ext.mapAsProfileMetadataPO
import net.primal.android.nostr.ext.mapNotNullAsPostDataPO
import net.primal.android.nostr.ext.mapNotNullAsPostStatsPO
import net.primal.android.nostr.ext.mapNotNullAsPostUserStatsPO
import net.primal.android.nostr.ext.mapNotNullAsRepostDataPO

suspend fun FeedResponse.persistToDatabaseAsTransaction(
    userId: String,
    database: PrimalDatabase,
) {
    val profiles = metadata.mapAsProfileMetadataPO()
    val feedPosts = posts.mapAsPostDataPO()
    val referencedPosts = referencedPosts.mapNotNullAsPostDataPO()
    val reposts = reposts.mapNotNullAsRepostDataPO()
    val postStats = primalEventStats.mapNotNullAsPostStatsPO()
    val userPostStats = primalEventUserStats.mapNotNullAsPostUserStatsPO(userId = userId)
    val primalMediaResources = primalEventResources.flatMapNotNullAsMediaResourcePO()

    val profileIdToProfileMetadataMap = profiles
        .groupBy { it.ownerId }
        .mapValues { it.value.first() }

    val allPosts = (feedPosts + referencedPosts).map { postData ->
        val eventIdMap = profileIdToProfileMetadataMap.mapValues { it.value.eventId }
        postData.copy(authorMetadataId = eventIdMap[postData.authorId])
    }

    database.withTransaction {
        database.profiles().upsertAll(profiles = profiles)
        database.posts().upsertAll(data = allPosts)
        database.mediaResources().upsertAll(data = allPosts.flatMapAsPostMediaResourcePO())
        database.nostrResources().upsertAll(data = allPosts.flatMapAsPostNostrResourcePO(
            postIdToPostDataMap = allPosts.groupBy { it.postId }.mapValues { it.value.first() },
            profileIdToProfileMetadataMap = profileIdToProfileMetadataMap
        ))
        database.reposts().upsertAll(data = reposts)
        database.postStats().upsertAll(data = postStats)
        database.postUserStats().upsertAll(data = userPostStats)
        database.mediaResources().upsertAll(data = primalMediaResources)
    }
}

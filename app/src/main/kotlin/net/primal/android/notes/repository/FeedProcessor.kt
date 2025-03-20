package net.primal.android.notes.repository

import androidx.room.withTransaction
import java.time.Instant
import net.primal.android.db.PrimalDatabase
import net.primal.android.notes.db.FeedPostDataCrossRef
import net.primal.android.notes.db.FeedPostRemoteKey
import net.primal.data.remote.api.feed.model.FeedResponse
import net.primal.data.remote.model.ContentPrimalPaging
import net.primal.domain.nostr.NostrEvent

class FeedProcessor(
    val feedSpec: String,
    val database: PrimalDatabase,
) {

    suspend fun processAndPersistToDatabase(
        userId: String,
        response: FeedResponse,
        clearFeed: Boolean,
    ) {
        val pagingEvent = response.paging
        database.withTransaction {
            if (clearFeed) {
                database.feedPostsRemoteKeys().deleteByDirective(ownerId = userId, directive = feedSpec)
                database.feedsConnections().deleteConnectionsByDirective(ownerId = userId, feedSpec = feedSpec)
            }

            response.persistToDatabaseAsTransaction(userId = userId, database = database)
            val feedEvents = response.notes + response.reposts
            feedEvents.processRemoteKeys(userId = userId, pagingEvent = pagingEvent)
            feedEvents.processFeedConnections(userId = userId)
        }
    }

    private suspend fun List<NostrEvent>.processRemoteKeys(userId: String, pagingEvent: ContentPrimalPaging?) {
        val sinceId = pagingEvent?.sinceId
        val untilId = pagingEvent?.untilId
        if (sinceId != null && untilId != null) {
            database.withTransaction {
                val remoteKeys = this.map {
                    FeedPostRemoteKey(
                        ownerId = userId,
                        eventId = it.id,
                        directive = feedSpec,
                        sinceId = sinceId,
                        untilId = untilId,
                        cachedAt = Instant.now().epochSecond,
                    )
                }

                database.feedPostsRemoteKeys().upsert(remoteKeys)
            }
        }
    }

    private suspend fun List<NostrEvent>.processFeedConnections(userId: String) {
        database.withTransaction {
            val maxIndex = database.feedsConnections().getOrderIndexForFeedSpec(ownerId = userId, feedSpec = feedSpec)
            val indexOffset = maxIndex?.plus(other = 1) ?: 0
            database.feedsConnections().connect(
                data = this.mapIndexed { index, nostrEvent ->
                    FeedPostDataCrossRef(
                        ownerId = userId,
                        feedSpec = feedSpec,
                        eventId = nostrEvent.id,
                        orderIndex = indexOffset + index,
                    )
                },
            )
        }
    }
}

package net.primal.data.remote.processors

import kotlinx.datetime.Clock
import net.primal.data.local.dao.notes.FeedPostDataCrossRef
import net.primal.data.local.dao.notes.FeedPostRemoteKey
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
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
        if (pagingEvent?.sinceId != null && pagingEvent.untilId != null) {
            database.withTransaction {
                val remoteKeys = this.map {
                    FeedPostRemoteKey(
                        ownerId = userId,
                        eventId = it.id,
                        directive = feedSpec,
                        sinceId = pagingEvent.sinceId,
                        untilId = pagingEvent.untilId,
                        cachedAt = Clock.System.now().epochSeconds,
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

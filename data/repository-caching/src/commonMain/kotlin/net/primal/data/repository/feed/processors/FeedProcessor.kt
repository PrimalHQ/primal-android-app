package net.primal.data.repository.feed.processors

import kotlinx.datetime.Clock
import net.primal.data.local.dao.notes.FeedPostDataCrossRef
import net.primal.data.local.dao.notes.FeedPostRemoteKey
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
import net.primal.data.remote.api.feed.model.FeedResponse
import net.primal.data.repository.mappers.remote.asFeedResponse
import net.primal.data.repository.mappers.remote.orderByPagingIfNotNull
import net.primal.domain.common.ContentPrimalPaging
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.posts.FeedPageSnapshot

internal class FeedProcessor(
    val feedSpec: String,
    val database: PrimalDatabase,
) {

    suspend fun processAndPersistToDatabase(
        userId: String,
        snapshot: FeedPageSnapshot,
        clearFeed: Boolean,
    ) = processAndPersistToDatabase(
        userId = userId,
        response = snapshot.asFeedResponse(),
        clearFeed = clearFeed,
    )

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
            feedEvents.orderByPagingIfNotNull(pagingEvent = pagingEvent)
                .processFeedConnections(userId = userId)
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
                        cachedAt = Clock.System.now().epochSeconds,
                    )
                }

                database.feedPostsRemoteKeys().upsert(remoteKeys)
            }
        }
    }

    private suspend fun List<NostrEvent>.processFeedConnections(userId: String) {
        database.withTransaction {
            database.feedsConnections().connect(
                data = this.map { nostrEvent ->
                    FeedPostDataCrossRef(
                        ownerId = userId,
                        feedSpec = feedSpec,
                        eventId = nostrEvent.id,
                    )
                },
            )
        }
    }
}

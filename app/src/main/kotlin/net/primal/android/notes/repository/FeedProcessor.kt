package net.primal.android.notes.repository

import androidx.room.withTransaction
import java.time.Instant
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalPaging
import net.primal.android.notes.api.model.FeedResponse
import net.primal.android.notes.db.FeedPostDataCrossRef
import net.primal.android.notes.db.FeedPostRemoteKey

class FeedProcessor(
    val feedDirective: String,
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
                database.feedPostsRemoteKeys().deleteByDirective(feedDirective)
                database.feedsConnections().deleteConnectionsByDirective(feedDirective)
                database.posts().deleteOrphanPosts()
            }

            response.persistToDatabaseAsTransaction(userId = userId, database = database)
            val feedEvents = response.posts + response.reposts
            feedEvents.processRemoteKeys(pagingEvent)
            feedEvents.processFeedConnections()
        }
    }

    private suspend fun List<NostrEvent>.processRemoteKeys(pagingEvent: ContentPrimalPaging?) {
        if (pagingEvent?.sinceId != null && pagingEvent.untilId != null) {
            database.withTransaction {
                val remoteKeys = this.map {
                    FeedPostRemoteKey(
                        eventId = it.id,
                        directive = feedDirective,
                        sinceId = pagingEvent.sinceId,
                        untilId = pagingEvent.untilId,
                        cachedAt = Instant.now().epochSecond,
                    )
                }

                database.feedPostsRemoteKeys().upsert(remoteKeys)
            }
        }
    }

    private fun List<NostrEvent>.processFeedConnections() {
        database.feedsConnections().connect(
            data = this.map {
                FeedPostDataCrossRef(
                    feedDirective = feedDirective,
                    eventId = it.id,
                )
            },
        )
    }
}

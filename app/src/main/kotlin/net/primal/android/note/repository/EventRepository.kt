package net.primal.android.note.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.note.api.EventStatsApi
import net.primal.android.note.api.model.EventZapsRequestBody
import net.primal.android.note.db.EventZap
import net.primal.android.note.reactions.mediator.EventZapsMediator
import timber.log.Timber

class EventRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val nostrPublisher: NostrPublisher,
    private val eventStatsApi: EventStatsApi,
    private val database: PrimalDatabase,
) {

    fun observeEventStats(eventIds: List<String>) = database.eventStats().observeStats(eventIds)

    fun observeUserEventStatus(eventIds: List<String>, userId: String) =
        database.eventUserStats().observeStats(eventIds, userId)

    @Throws(NostrPublishException::class)
    suspend fun likeEvent(
        userId: String,
        eventId: String,
        eventAuthorId: String,
        optionalTags: List<JsonArray> = emptyList(),
    ) = withContext(dispatcherProvider.io()) {
        val statsUpdater = EventStatsUpdater(
            eventId = eventId,
            userId = userId,
            eventAuthorId = eventAuthorId,
            database = database,
        )

        try {
            statsUpdater.increaseLikeStats()
            nostrPublisher.signAndPublishNostrEvent(
                userId = userId,
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = NostrEventKind.Reaction.value,
                    tags = listOf(eventId.asEventIdTag(), eventAuthorId.asPubkeyTag()) + optionalTags,
                    content = "+",
                ),
            )
        } catch (error: NostrPublishException) {
            Timber.w(error)
            statsUpdater.revertStats()
            throw error
        }
    }

    @Throws(NostrPublishException::class)
    suspend fun repostEvent(
        userId: String,
        eventId: String,
        eventKind: NostrEventKind,
        eventAuthorId: String,
        eventRawNostrEvent: String,
        optionalTags: List<JsonArray> = emptyList(),
    ) = withContext(dispatcherProvider.io()) {
        val statsUpdater = EventStatsUpdater(
            eventId = eventId,
            userId = userId,
            eventAuthorId = eventAuthorId,
            database = database,
        )

        try {
            statsUpdater.increaseRepostStats()
            nostrPublisher.signAndPublishNostrEvent(
                userId = userId,
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = if (eventKind == NostrEventKind.ShortTextNote) {
                        NostrEventKind.ShortTextNoteRepost.value
                    } else {
                        NostrEventKind.GenericRepost.value
                    },
                    tags = listOf(eventId.asEventIdTag(), eventAuthorId.asPubkeyTag()) + optionalTags,
                    content = eventRawNostrEvent,
                ),
            )
        } catch (error: NostrPublishException) {
            Timber.w(error)
            statsUpdater.revertStats()
            throw error
        }
    }

    suspend fun fetchEventZaps(
        userId: String,
        eventId: String,
        limit: Int,
    ) = withContext(dispatcherProvider.io()) {
        val response = eventStatsApi.getEventZaps(
            EventZapsRequestBody(
                eventId = eventId,
                userId = userId,
                limit = limit,
            ),
        )
        response.persistToDatabaseAsTransaction(database = database)
    }

    fun pagedEventZaps(userId: String, eventId: String): Flow<PagingData<EventZap>> {
        return createPager(userId = userId, eventId = eventId) {
            database.eventZaps().pagedEventZaps(eventId = eventId)
        }.flow
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(
        userId: String,
        eventId: String,
        pagingSourceFactory: () -> PagingSource<Int, EventZap>,
    ) = Pager(
        config = PagingConfig(
            pageSize = 50,
            prefetchDistance = 50,
            initialLoadSize = 150,
            enablePlaceholders = true,
        ),
        remoteMediator = EventZapsMediator(
            eventId = eventId,
            userId = userId,
            dispatcherProvider = dispatcherProvider,
            eventStatsApi = eventStatsApi,
            database = database,
        ),
        pagingSourceFactory = pagingSourceFactory,
    )
}

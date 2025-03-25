package net.primal.android.events.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.events.db.EventZap
import net.primal.android.events.domain.EventAction
import net.primal.android.events.reactions.mediator.EventZapsMediator
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsMapPubkeyToListOfBlossomServers
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.parseAndMapPrimalLegendProfiles
import net.primal.android.nostr.ext.parseAndMapPrimalPremiumInfo
import net.primal.android.nostr.ext.parseAndMapPrimalUserNames
import net.primal.android.nostr.ext.takeContentAsPrimalUserScoresOrNull
import net.primal.android.nostr.notary.MissingPrivateKeyException
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.data.remote.api.events.EventStatsApi
import net.primal.data.remote.api.events.model.EventActionsRequestBody
import net.primal.data.remote.api.events.model.EventZapsRequestBody
import net.primal.domain.nostr.NostrEventKind
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

    @Throws(NostrPublishException::class, MissingPrivateKeyException::class)
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
            nostrPublisher.signPublishImportNostrEvent(
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
        } catch (error: MissingPrivateKeyException) {
            Timber.w(error)
            statsUpdater.revertStats()
            throw error
        }
    }

    @Throws(NostrPublishException::class, MissingPrivateKeyException::class)
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
            nostrPublisher.signPublishImportNostrEvent(
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
        } catch (error: MissingPrivateKeyException) {
            Timber.w(error)
            statsUpdater.revertStats()
            throw error
        }
    }

    suspend fun fetchEventActions(eventId: String, kind: Int): List<EventAction> =
        withContext(dispatcherProvider.io()) {
            val response = eventStatsApi.getEventActions(
                body = EventActionsRequestBody(
                    eventId = eventId,
                    kind = kind,
                    limit = 100,
                ),
            )

            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource()
            val primalNames = response.primalUserNames.parseAndMapPrimalUserNames()
            val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
            val profiles = response.profiles.mapAsProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalNames,
                primalPremiumInfo = primalPremiumInfo,
                primalLegendProfiles = primalLegendProfiles,
                blossomServers = blossomServers,
            )
            database.withTransaction {
                database.profiles().insertOrUpdateAll(data = profiles)
            }

            val userScoresMap = response.userScores?.takeContentAsPrimalUserScoresOrNull()
            val profilesMap = profiles.asMapByKey { it.ownerId }
            response.actions.mapNotNull { action ->
                profilesMap[action.pubKey]?.let { profileData ->
                    EventAction(
                        profile = profileData,
                        score = userScoresMap?.get(action.pubKey) ?: 0f,
                        actionEventData = action,
                        actionEventKind = action.kind,
                    )
                }
            }.sortedByDescending { it.score }.distinctBy { it.profile }
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

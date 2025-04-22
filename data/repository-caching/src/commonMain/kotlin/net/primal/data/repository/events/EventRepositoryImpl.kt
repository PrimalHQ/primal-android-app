package net.primal.data.repository.events

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.asMapByKey
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.events.EventZap as EventZapPO
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
import net.primal.data.remote.api.events.EventStatsApi
import net.primal.data.remote.api.events.model.EventActionsRequestBody
import net.primal.data.remote.api.events.model.EventZapsRequestBody
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.events.paging.EventZapsMediator
import net.primal.data.repository.events.processors.persistToDatabaseAsTransaction
import net.primal.data.repository.mappers.local.asEventZapDO
import net.primal.data.repository.mappers.local.asNostrEventStats
import net.primal.data.repository.mappers.local.asNostrEventUserStats
import net.primal.data.repository.mappers.local.asProfileDataDO
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.data.repository.mappers.remote.takeContentAsPrimalUserScoresOrNull
import net.primal.domain.events.EventRepository
import net.primal.domain.events.EventZap as EventZapDO
import net.primal.domain.events.NostrEventAction

class EventRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val eventStatsApi: EventStatsApi,
    private val database: PrimalDatabase,
) : EventRepository {

    override fun observeEventStats(eventIds: List<String>) =
        database.eventStats().observeStats(eventIds).map { it.map { it.asNostrEventStats() } }

    override fun observeUserEventStatus(eventIds: List<String>, userId: String) =
        database.eventUserStats().observeStats(eventIds, userId).map { it.map { it.asNostrEventUserStats() } }

    override suspend fun fetchEventActions(eventId: String, kind: Int): List<NostrEventAction> =
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
                    NostrEventAction(
                        profile = profileData.asProfileDataDO(),
                        score = userScoresMap?.get(action.pubKey) ?: 0f,
                        actionEventData = action,
                        actionEventKind = action.kind,
                    )
                }
            }.sortedByDescending { it.score }.distinctBy { it.profile }
        }

    override suspend fun fetchEventZaps(
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

    override fun pagedEventZaps(userId: String, eventId: String): Flow<PagingData<EventZapDO>> {
        return createPager(userId = userId, eventId = eventId) {
            database.eventZaps().pagedEventZaps(eventId = eventId)
        }.flow.map { it.map { it.asEventZapDO() } }
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(
        userId: String,
        eventId: String,
        pagingSourceFactory: () -> PagingSource<Int, EventZapPO>,
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

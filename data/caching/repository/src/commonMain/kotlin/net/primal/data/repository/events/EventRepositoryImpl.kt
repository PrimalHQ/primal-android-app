package net.primal.data.repository.events

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.caching.MediaCacher
import net.primal.core.utils.CurrencyConversionUtils.toBigDecimal
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.core.utils.Result
import net.primal.core.utils.asMapByKey
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.map
import net.primal.core.utils.mapCatching
import net.primal.core.utils.recover
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.core.utils.toDouble
import net.primal.data.local.dao.events.EventZap as EventZapPO
import net.primal.data.local.dao.events.eventRelayHintsUpserter
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.events.EventStatsApi
import net.primal.data.remote.api.events.model.EventActionsRequestBody
import net.primal.data.remote.api.events.model.EventZapsRequestBody
import net.primal.data.remote.api.events.model.ReplaceableEventResponse
import net.primal.data.remote.api.events.model.ReplaceableEventsRequest
import net.primal.data.remote.api.events.model.toReplaceableEventRequest
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.events.paging.EventZapsMediator
import net.primal.data.repository.events.processors.persistToDatabaseAsTransaction
import net.primal.data.repository.mappers.local.asEventZapDO
import net.primal.data.repository.mappers.local.asNostrEventStats
import net.primal.data.repository.mappers.local.asNostrEventUserStats
import net.primal.data.repository.mappers.local.asProfileDataDO
import net.primal.data.repository.mappers.remote.flatMapAsEventHintsPO
import net.primal.data.repository.mappers.remote.flatMapAsWordCount
import net.primal.data.repository.mappers.remote.mapAsEventZapDO
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.mapNotNullAsArticleDataPO
import net.primal.data.repository.mappers.remote.mapNotNullAsEventStatsPO
import net.primal.data.repository.mappers.remote.mapNotNullAsStreamDataPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.data.repository.mappers.remote.takeContentAsPrimalUserScoresOrNull
import net.primal.data.repository.utils.cacheAvatarUrls
import net.primal.domain.events.EventRepository
import net.primal.domain.events.EventZap as EventZapDO
import net.primal.domain.events.NostrEventAction
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.findFirstATag
import net.primal.domain.nostr.findFirstEventId
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.nostr.findFirstZapAmount
import net.primal.domain.nostr.utils.LnInvoiceUtils
import net.primal.shared.data.local.db.withTransaction

class EventRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val eventStatsApi: EventStatsApi,
    private val database: PrimalDatabase,
    private val mediaCacher: MediaCacher? = null,
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
            mediaCacher?.cacheAvatarUrls(metadata = response.profiles, cdnResources = response.cdnResources)

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
        mediaCacher?.cacheAvatarUrls(metadata = response.profiles, cdnResources = response.cdnResources)
        response.persistToDatabaseAsTransaction(database = database)
    }

    override fun pagedEventZaps(
        userId: String,
        eventId: String,
        articleATag: String?,
    ): Flow<PagingData<EventZapDO>> {
        return createPager(userId = userId, eventId = eventId) {
            database.eventZaps().pagedEventZaps(eventId = articleATag ?: eventId)
        }.flow.map { it.map { it.asEventZapDO() } }
            .flowOn(dispatcherProvider.io())
    }

    override suspend fun observeZapsByEventId(eventId: String): Flow<List<EventZapDO>> =
        withContext(dispatcherProvider.io()) {
            database.eventZaps().observeAllByEventId(eventId = eventId)
                .distinctUntilChanged()
                .map { list -> list.map { it.asEventZapDO() } }
        }

    override suspend fun fetchReplaceableEvent(naddr: Naddr): Result<Unit> =
        withContext(dispatcherProvider.io()) {
            runCatching {
                val response = eventStatsApi.getReplaceableEvent(body = naddr.toReplaceableEventRequest())

                persistReplaceableEventResponse(response = response.getOrThrow())
            }
        }

    override suspend fun fetchReplaceableEvents(naddrs: List<Naddr>): Result<Unit> =
        withContext(dispatcherProvider.io()) {
            runCatching {
                val response = eventStatsApi.getReplaceableEvents(
                    body = ReplaceableEventsRequest(events = naddrs.map { it.toReplaceableEventRequest() }),
                )

                persistReplaceableEventResponse(response = response.getOrThrow())
            }
        }

    private suspend fun persistReplaceableEventResponse(response: ReplaceableEventResponse) {
        mediaCacher?.cacheAvatarUrls(metadata = response.metadata, cdnResources = response.cdnResources)
        val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource()
        val eventHints = response.relayHints.flatMapAsEventHintsPO()
        val wordsCountMap = response.wordCount.flatMapAsWordCount()

        val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
        val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
        val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()

        val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()

        val profiles = response.metadata.mapAsProfileDataPO(
            cdnResources = cdnResources,
            primalUserNames = primalUserNames,
            primalPremiumInfo = primalPremiumInfo,
            primalLegendProfiles = primalLegendProfiles,
            blossomServers = blossomServers,
        )

        val streamData = response.liveActivity.mapNotNullAsStreamDataPO()

        val articles = response.articles.mapNotNullAsArticleDataPO(
            wordsCountMap = wordsCountMap,
            cdnResources = cdnResources,
        )

        val eventStats = response.eventStats.mapNotNullAsEventStatsPO()

        database.withTransaction {
            database.eventStats().upsertAll(eventStats)
            database.streams().upsertStreamData(streamData)
            database.profiles().insertOrUpdateAll(profiles)
            database.articles().upsertAll(articles)

            val hintsMap = eventHints.associateBy { it.eventId }
            eventRelayHintsUpserter(dao = database.eventHints(), eventIds = eventHints.map { it.eventId }) {
                copy(relays = hintsMap[this.eventId]?.relays ?: emptyList())
            }
        }
    }

    override suspend fun getZapReceipts(invoices: List<String>): Result<Map<String, NostrEvent>> =
        withContext(dispatcherProvider.io()) {
            if (invoices.isEmpty()) return@withContext Result.success(emptyMap())

            val localZapReceipts = database.eventZaps().findAllByInvoices(invoices = invoices)
            val localMap = localZapReceipts.mapNotNull { zapEvent ->
                val invoice = zapEvent.invoice
                val nostrEvent = zapEvent.rawNostrEvent?.decodeFromJsonStringOrNull<NostrEvent>()
                if (invoice != null && nostrEvent != null) invoice to nostrEvent else null
            }.toMap()

            val missingZapReceiptsByInvoice = invoices.toSet() - localZapReceipts.mapNotNull { it.invoice }.toSet()

            if (missingZapReceiptsByInvoice.isNotEmpty()) {
                eventStatsApi.getZapReceipts(invoices = missingZapReceiptsByInvoice.toList())
                    .mapCatching { response ->
                        val map = response.mapEvent?.content?.decodeFromJsonStringOrNull<Map<String, NostrEvent>>()
                            ?: throw IllegalArgumentException("failed to parse invoices map.")

                        database.eventZaps()
                            .upsertAll(data = map.values.toList().mapAsEventZapDO(profilesMap = emptyMap()))

                        map
                    }
            } else {
                Result.success(emptyMap())
            }.map { it + localMap }.recover { localMap }
        }

    override suspend fun saveZapRequest(invoice: String, zapRequestEvent: NostrEvent) =
        withContext(dispatcherProvider.io()) {
            val senderId = zapRequestEvent.pubKey
            val receiverId = zapRequestEvent.tags.findFirstProfileId() ?: return@withContext
            val eventId = zapRequestEvent.tags.findFirstATag()
                ?: zapRequestEvent.tags.findFirstEventId()
                ?: receiverId

            val amountInSats = zapRequestEvent.tags.findFirstZapAmount()?.toBigDecimal()
                ?: LnInvoiceUtils.getAmountInSatsOrNull(invoice)
                ?: return@withContext

            val data = EventZapPO(
                eventId = eventId,
                zapSenderId = senderId,
                zapReceiverId = receiverId,
                zapRequestAt = zapRequestEvent.createdAt,
                zapReceiptAt = 0,
                amountInBtc = amountInSats.toBtc().toDouble(),
                message = zapRequestEvent.content,
                invoice = invoice,
                rawNostrEvent = zapRequestEvent.encodeToJsonString(),
            )
            database.eventZaps().upsertAll(data = listOf(data))
        }

    override suspend fun deleteZapRequest(invoice: String) =
        withContext(dispatcherProvider.io()) {
            database.eventZaps().deleteByInvoice(invoice = invoice)
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
            mediaCacher = mediaCacher,
        ),
        pagingSourceFactory = pagingSourceFactory,
    )
}

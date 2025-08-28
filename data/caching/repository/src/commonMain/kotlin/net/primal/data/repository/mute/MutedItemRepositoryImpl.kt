package net.primal.data.repository.mute

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.data.local.dao.mutes.ListType
import net.primal.data.local.dao.mutes.MutedItemData
import net.primal.data.local.dao.mutes.MutedItemType
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.settings.SettingsApi
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.mappers.local.asProfileDataDO
import net.primal.data.repository.mappers.remote.asProfileDataPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.domain.mutes.MutedItemRepository
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asEventIdTag
import net.primal.domain.nostr.asHashtagTag
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.asWordTag
import net.primal.domain.nostr.getTagValueOrNull
import net.primal.domain.nostr.isEventIdTag
import net.primal.domain.nostr.isHashtagTag
import net.primal.domain.nostr.isPubKeyTag
import net.primal.domain.nostr.isWordTag
import net.primal.domain.publisher.PrimalPublisher
import net.primal.shared.data.local.db.withTransaction

class MutedItemRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: PrimalDatabase,
    private val settingsApi: SettingsApi,
    private val primalPublisher: PrimalPublisher,
) : MutedItemRepository {
    override fun observeMutedUsersByOwnerId(ownerId: String) =
        database.mutedItems().observeMutedUsersByOwnerId(ownerId = ownerId)
            .map { it.mapNotNull { it.profileData?.asProfileDataDO() } }

    override fun observeIsUserMutedByOwnerId(pubkey: String, ownerId: String) =
        database.mutedItems().observeIsUserMutedByOwnerId(
            pubkey = pubkey,
            ownerId = ownerId,
        )

    override fun observeMutedHashtagsByOwnerId(ownerId: String) =
        database.mutedItems()
            .observeMutedItemsByType(ownerId = ownerId, type = MutedItemType.Hashtag)
            .map { it.map { item -> item.item } }

    override fun observeMutedProfileIdsByOwnerId(ownerId: String) =
        database.mutedItems().observeMutedProfileIdsByOwnerId(ownerId = ownerId)

    override fun observeMutedWordsByOwnerId(ownerId: String) =
        database.mutedItems()
            .observeMutedItemsByType(ownerId = ownerId, type = MutedItemType.Word)
            .map { it.map { item -> item.item } }

    override fun observeIsStreamMutedByOwnerId(pubkey: String, ownerId: String) =
        database.mutedItems()
            .observeIsStreamMutedByOwnerId(ownerId = ownerId, pubkey = pubkey)

    override suspend fun fetchAndPersistMuteList(userId: String) {
        val muteList = fetchMuteListAndPersistProfiles(userId = userId)
        persistList(ownerId = userId, listType = ListType.MuteList, muteList = muteList)
    }

    override suspend fun fetchAndPersistStreamMuteList(userId: String) =
        runCatching {
            val muteList = fetchStreamMuteListAndPersistProfiles(userId = userId).getOrThrow()
            persistList(ownerId = userId, listType = ListType.StreamMuteList, muteList = muteList)
        }

    override suspend fun muteStreamNotifications(ownerId: String, pubkey: String): Result<Unit> =
        updateAndPersistStreamMuteList(userId = ownerId) {
            plus(
                MutedItemData(
                    ownerId = ownerId,
                    item = pubkey,
                    type = MutedItemType.Stream,
                    listType = ListType.StreamMuteList,
                ),
            )
        }

    override suspend fun unmuteStreamNotifications(ownerId: String, pubkey: String): Result<Unit> =
        updateAndPersistStreamMuteList(userId = ownerId) {
            minus(
                MutedItemData(
                    ownerId = ownerId,
                    item = pubkey,
                    type = MutedItemType.Stream,
                    listType = ListType.StreamMuteList,
                ),
            )
        }

    override suspend fun muteUserAndPersistMuteList(userId: String, mutedUserId: String) =
        updateAndPersistMuteList(userId = userId) {
            plus(
                MutedItemData(
                    item = mutedUserId,
                    ownerId = userId,
                    type = MutedItemType.User,
                    listType = ListType.MuteList,
                ),
            )
        }

    override suspend fun unmuteUserAndPersistMuteList(userId: String, unmutedUserId: String) =
        updateAndPersistMuteList(userId = userId) {
            minus(
                MutedItemData(
                    item = unmutedUserId,
                    ownerId = userId,
                    type = MutedItemType.User,
                    listType = ListType.MuteList,
                ),
            )
        }

    override suspend fun muteThreadAndPersistMuteList(userId: String, postId: String) {
        updateAndPersistMuteList(userId = userId) {
            plus(
                MutedItemData(
                    item = postId,
                    ownerId = userId,
                    type = MutedItemType.Thread,
                    listType = ListType.MuteList,
                ),
            )
        }
    }

    override suspend fun unmuteThreadAndPersistMuteList(userId: String, postId: String) {
        updateAndPersistMuteList(userId = userId) {
            minus(
                MutedItemData(
                    item = postId,
                    ownerId = userId,
                    type = MutedItemType.Thread,
                    listType = ListType.MuteList,
                ),
            )
        }
    }

    override suspend fun muteHashtagAndPersistMuteList(userId: String, hashtag: String) =
        updateAndPersistMuteList(userId = userId) {
            plus(
                MutedItemData(
                    item = hashtag,
                    ownerId = userId,
                    type = MutedItemType.Hashtag,
                    listType = ListType.MuteList,
                ),
            )
        }

    override suspend fun unmuteHashtagAndPersistMuteList(userId: String, hashtag: String) =
        updateAndPersistMuteList(userId = userId) {
            minus(
                MutedItemData(
                    item = hashtag,
                    ownerId = userId,
                    type = MutedItemType.Hashtag,
                    listType = ListType.MuteList,
                ),
            )
        }

    override suspend fun muteWordAndPersistMuteList(userId: String, word: String) =
        updateAndPersistMuteList(userId = userId) {
            plus(MutedItemData(item = word, ownerId = userId, type = MutedItemType.Word, listType = ListType.MuteList))
        }

    override suspend fun unmuteWordAndPersistMuteList(userId: String, word: String) =
        updateAndPersistMuteList(userId = userId) {
            minus(MutedItemData(item = word, ownerId = userId, type = MutedItemType.Word, listType = ListType.MuteList))
        }

    private suspend fun updateAndPersistMuteList(
        userId: String,
        reducer: Set<MutedItemData>.() -> Set<MutedItemData>,
    ) = withContext(dispatcherProvider.io()) {
        val remoteMuteList = fetchMuteListAndPersistProfiles(userId = userId)
        val newMuteList = remoteMuteList.reducer()

        primalPublisher.signPublishImportNostrEvent(
            NostrUnsignedEvent(
                content = "",
                pubKey = userId,
                kind = NostrEventKind.MuteList.value,
                tags = newMuteList.map { it.toTag() },
            ),
        )
        persistList(ownerId = userId, listType = ListType.MuteList, muteList = newMuteList)
    }

    private suspend fun updateAndPersistStreamMuteList(
        userId: String,
        reducer: Set<MutedItemData>.() -> Set<MutedItemData>,
    ) = withContext(dispatcherProvider.io()) {
        runCatching {
            val remoteStreamMuteList = fetchStreamMuteListAndPersistProfiles(userId = userId).getOrThrow()
            val newMuteList = remoteStreamMuteList.reducer()

            primalPublisher.signPublishImportNostrEvent(
                unsignedNostrEvent = NostrUnsignedEvent(
                    content = "",
                    pubKey = userId,
                    kind = NostrEventKind.StreamMuteList.value,
                    tags = newMuteList.map { it.toTag() },
                ),
            )

            persistList(ownerId = userId, listType = ListType.StreamMuteList, muteList = newMuteList)
        }
    }

    private suspend fun fetchStreamMuteListAndPersistProfiles(userId: String): Result<Set<MutedItemData>> =
        runCatching {
            val response = settingsApi.getStreamMuteList(userId = userId).getOrThrow()

            val muteList = response.streamMuteList
                ?.tags?.mapNotNull { it.toMutedItemData(ownerId = userId, listType = ListType.StreamMuteList) }?.toSet()
                ?: emptySet()

            val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
            val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource()
            val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
            val profileData = response.metadataEvents.map {
                it.asProfileDataPO(
                    cdnResources = cdnResources,
                    primalUserNames = primalUserNames,
                    primalPremiumInfo = primalPremiumInfo,
                    primalLegendProfiles = primalLegendProfiles,
                    blossomServers = blossomServers,
                )
            }

            database.profiles().insertOrUpdateAll(data = profileData)

            muteList
        }

    private suspend fun fetchMuteListAndPersistProfiles(userId: String): Set<MutedItemData> {
        val response = settingsApi.getMuteList(userId = userId)
        val muteList = response.muteList
            ?.tags?.mapNotNull { it.toMutedItemData(ownerId = userId, listType = ListType.MuteList) }?.toSet()
            ?: emptySet()

        val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
        val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
        val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
        val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource()
        val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
        val profileData = response.metadataEvents.map {
            it.asProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserNames,
                primalPremiumInfo = primalPremiumInfo,
                primalLegendProfiles = primalLegendProfiles,
                blossomServers = blossomServers,
            )
        }

        database.profiles().insertOrUpdateAll(data = profileData)

        return muteList
    }

    private suspend fun persistList(
        ownerId: String,
        listType: ListType,
        muteList: Set<MutedItemData>,
    ) = withContext(dispatcherProvider.io()) {
        database.withTransaction {
            database.mutedItems().deleteListByOwnerId(ownerId = ownerId, listType = listType)
            database.mutedItems().upsertAll(data = muteList)
        }
    }

    private fun JsonArray.toMutedItemData(ownerId: String, listType: ListType) =
        getTagValueOrNull()?.let { value ->
            findMutedType(listType = listType)?.let { type ->
                MutedItemData(
                    item = value,
                    ownerId = ownerId,
                    type = type,
                    listType = listType,
                )
            }
        }

    private fun MutedItemData.toTag() =
        when (this.type) {
            MutedItemType.User -> this.item.asPubkeyTag()
            MutedItemType.Hashtag -> this.item.asHashtagTag()
            MutedItemType.Word -> this.item.asWordTag()
            MutedItemType.Thread -> this.item.asEventIdTag()
            MutedItemType.Stream -> this.item.asPubkeyTag()
        }

    private fun JsonArray.findMutedType(listType: ListType) =
        when (listType) {
            ListType.MuteList ->
                when {
                    isPubKeyTag() -> MutedItemType.User
                    isEventIdTag() -> MutedItemType.Thread
                    isHashtagTag() -> MutedItemType.Hashtag
                    isWordTag() -> MutedItemType.Word

                    else -> null
                }

            ListType.StreamMuteList -> when {
                isPubKeyTag() -> MutedItemType.Stream
                else -> null
            }
        }
}

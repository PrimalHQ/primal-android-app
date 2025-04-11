package net.primal.data.repository.mute

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.profiles.MutedUserData
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
import net.primal.data.remote.api.settings.SettingsApi
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.mappers.local.asProfileDataDO
import net.primal.data.repository.mappers.remote.asProfileDataPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.profile.MutedUserRepository
import net.primal.domain.publisher.PrimalPublisher

class MutedUserRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: PrimalDatabase,
    private val settingsApi: SettingsApi,
    private val primalPublisher: PrimalPublisher,
) : MutedUserRepository {

    override fun observeMutedUsersByOwnerId(ownerId: String) =
        database.mutedUsers()
            .observeMutedUsersByOwnerId(ownerId = ownerId)
            .map { it.mapNotNull { it.profileData?.asProfileDataDO() } }

    override fun observeIsUserMutedByOwnerId(pubkey: String, ownerId: String) =
        database.mutedUsers().observeIsUserMutedByOwnerId(
            pubkey = pubkey,
            ownerId = ownerId,
        )

    override suspend fun fetchAndPersistMuteList(userId: String) {
        val muteList = fetchMuteListAndPersistProfiles(userId = userId)
        persistMuteList(ownerId = userId, muteList = muteList)
    }

    override suspend fun muteUserAndPersistMuteList(userId: String, mutedUserId: String) =
        withContext(dispatcherProvider.io()) {
            val userMetadataEventId = database.profiles().findMetadataEventId(mutedUserId)
            updateAndPersistMuteList(userId = userId) {
                toMutableSet().apply {
                    add(
                        MutedUserData(
                            userId = mutedUserId,
                            userMetadataEventId = userMetadataEventId,
                            ownerId = userId,
                        ),
                    )
                }
            }
        }

    override suspend fun unmuteUserAndPersistMuteList(userId: String, unmutedUserId: String) {
        updateAndPersistMuteList(userId = userId) {
            filterNot { it.userId == unmutedUserId && it.ownerId == userId }.toSet()
        }
    }

    private suspend fun updateAndPersistMuteList(
        userId: String,
        reducer: Set<MutedUserData>.() -> Set<MutedUserData>,
    ) = withContext(dispatcherProvider.io()) {
        val remoteMuteList = fetchMuteListAndPersistProfiles(userId = userId)
        val newMuteList = remoteMuteList.reducer()
        primalPublisher.signPublishImportNostrEvent(
            NostrUnsignedEvent(
                content = "",
                pubKey = userId,
                kind = NostrEventKind.MuteList.value,
                tags = newMuteList
                    .map { it.userId }
                    .toSet()
                    .map { it.asPubkeyTag() },
            ),
        )
        persistMuteList(ownerId = userId, muteList = newMuteList)
    }

    private suspend fun fetchMuteListAndPersistProfiles(userId: String): Set<MutedUserData> {
        val response = settingsApi.getMuteList(userId = userId)
        val muteList = response.muteList?.tags?.mapToPubkeySet() ?: emptySet()
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
            .map { mutedUserId ->
                mutedUserId.asMutedAccountPO(
                    metadataEventId = profileData.find { mutedUserId == it.ownerId }?.eventId,
                    ownerId = userId,
                )
            }
            .toSet()
    }

    private suspend fun persistMuteList(ownerId: String, muteList: Set<MutedUserData>) {
        withContext(dispatcherProvider.io()) {
            database.withTransaction {
                database.mutedUsers().deleteAllByOwnerId(ownerId = ownerId)
                database.mutedUsers().upsertAll(data = muteList)
            }
        }
    }

    private fun String.asMutedAccountPO(metadataEventId: String? = null, ownerId: String): MutedUserData =
        MutedUserData(userId = this, userMetadataEventId = metadataEventId, ownerId = ownerId)

    private fun List<JsonArray>?.mapToPubkeySet(): Set<String>? {
        return this?.filter { it.size == 2 }?.map { it[1].jsonPrimitive.content }?.toSet()
    }
}

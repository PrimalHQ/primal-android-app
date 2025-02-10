package net.primal.android.settings.muted.repository

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsMapPubkeyToListOfBlossomServers
import net.primal.android.nostr.ext.parseAndMapPrimalLegendProfiles
import net.primal.android.nostr.ext.parseAndMapPrimalPremiumInfo
import net.primal.android.nostr.ext.parseAndMapPrimalUserNames
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.settings.api.SettingsApi
import net.primal.android.settings.muted.db.MutedUserData

class MutedUserRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val database: PrimalDatabase,
    private val settingsApi: SettingsApi,
    private val nostrPublisher: NostrPublisher,
) {

    fun observeMutedUsersByOwnerId(ownerId: String) =
        database.mutedUsers().observeMutedUsersByOwnerId(ownerId = ownerId)

    fun observeIsUserMutedByOwnerId(pubkey: String, ownerId: String) =
        database.mutedUsers().observeIsUserMutedByOwnerId(
            pubkey = pubkey,
            ownerId = ownerId,
        )

    suspend fun fetchAndPersistMuteList(userId: String) {
        val muteList = fetchMuteListAndPersistProfiles(userId = userId)
        persistMuteList(ownerId = userId, muteList = muteList)
    }

    @Throws(MissingRelaysException::class)
    suspend fun muteUserAndPersistMuteList(userId: String, mutedUserId: String) =
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

    @Throws(MissingRelaysException::class)
    suspend fun unmuteUserAndPersistMuteList(userId: String, unmutedUserId: String) {
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
        nostrPublisher.setMuteList(userId = userId, muteList = newMuteList.map { it.userId }.toSet())
        persistMuteList(ownerId = userId, muteList = newMuteList)
    }

    private suspend fun fetchMuteListAndPersistProfiles(userId: String): Set<MutedUserData> {
        val response = settingsApi.getMuteList(userId = userId)
        val muteList = response.muteList?.tags?.mapToPubkeySet() ?: emptySet()
        val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
        val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
        val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
        val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
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
        database.withTransaction {
            database.mutedUsers().deleteAllByOwnerId(ownerId = ownerId)
            database.mutedUsers().upsertAll(data = muteList)
        }
    }

    private fun String.asMutedAccountPO(metadataEventId: String? = null, ownerId: String): MutedUserData =
        MutedUserData(userId = this, userMetadataEventId = metadataEventId, ownerId = ownerId)

    private fun List<JsonArray>?.mapToPubkeySet(): Set<String>? {
        return this?.filter { it.size == 2 }?.map { it[1].jsonPrimitive.content }?.toSet()
    }
}

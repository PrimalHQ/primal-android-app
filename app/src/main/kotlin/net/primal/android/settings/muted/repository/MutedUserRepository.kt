package net.primal.android.settings.muted.repository

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.settings.api.SettingsApi
import net.primal.android.settings.muted.db.MutedUserData

class MutedUserRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val settingsApi: SettingsApi,
    private val nostrPublisher: NostrPublisher,
) {

    fun observeMutedUsers() = database.mutedUsers().observeMutedUsers()

    fun observeIsUserMuted(pubkey: String) =
        database.mutedUsers().observeIsUserMuted(
            pubkey = pubkey,
        )

    suspend fun fetchAndPersistMuteList(userId: String) {
        val muteList = fetchMuteListAndPersistProfiles(userId = userId)
        persistMuteList(muteList = muteList)
    }

    @Throws(MissingRelaysException::class)
    suspend fun muteUserAndPersistMuteList(userId: String, mutedUserId: String) {
        val userMetadataEventId = database.profiles().findMetadataEventId(mutedUserId)
        updateAndPersistMuteList(userId = userId) {
            toMutableSet().apply {
                add(
                    MutedUserData(
                        userId = mutedUserId,
                        userMetadataEventId = userMetadataEventId,
                    ),
                )
            }
        }
    }

    @Throws(MissingRelaysException::class)
    suspend fun unmuteUserAndPersistMuteList(userId: String, unmutedUserId: String) {
        updateAndPersistMuteList(userId = userId) {
            toMutableSet().apply {
                removeIf { it.userId == unmutedUserId }
            }
        }
    }

    private suspend fun updateAndPersistMuteList(
        userId: String,
        reducer: Set<MutedUserData>.() -> Set<MutedUserData>,
    ) {
        val remoteMuteList = fetchMuteListAndPersistProfiles(userId = userId)
        val newMuteList = remoteMuteList.reducer()
        nostrPublisher.setMuteList(userId = userId, muteList = newMuteList.map { it.userId }.toSet())
        persistMuteList(muteList = newMuteList)
    }

    private suspend fun fetchMuteListAndPersistProfiles(userId: String): Set<MutedUserData> {
        val response = settingsApi.getMuteList(userId = userId)
        val muteList = response.muteList?.tags?.mapToPubkeySet() ?: emptySet()
        val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val profileData = response.metadataEvents.map { it.asProfileDataPO(cdnResources = cdnResources) }

        database.profiles().upsertAll(data = profileData)
        return muteList
            .map { mutedUserId ->
                mutedUserId.asMutedAccountPO(
                    metadataEventId = profileData.find { mutedUserId == it.ownerId }?.eventId,
                )
            }
            .toSet()
    }

    private suspend fun persistMuteList(muteList: Set<MutedUserData>) {
        database.withTransaction {
            database.mutedUsers().deleteAll()
            database.mutedUsers().upsertAll(data = muteList)
        }
    }

    private fun String.asMutedAccountPO(metadataEventId: String? = null): MutedUserData =
        MutedUserData(userId = this, userMetadataEventId = metadataEventId)

    private fun List<JsonArray>?.mapToPubkeySet(): Set<String>? {
        return this?.filter { it.size == 2 }?.map { it[1].jsonPrimitive.content }?.toSet()
    }
}

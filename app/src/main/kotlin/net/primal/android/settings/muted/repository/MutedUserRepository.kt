package net.primal.android.settings.muted.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.map
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.settings.api.SettingsApi
import net.primal.android.settings.muted.db.Muted
import net.primal.android.settings.muted.model.MutedUser
import net.primal.android.user.api.UsersApi
import javax.inject.Inject

class MutedUserRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val settingsApi: SettingsApi,
    private val usersApi: UsersApi
) {
    val mutedUsers = database.muted().observeAllMuted().map {
        val response = usersApi.getUserProfiles(it.map { muted -> muted.pubkey }.toSet())

        return@map response.map { r ->
            val profileData = r.asProfileDataPO()

            MutedUser(
                name = profileData.usernameUiFriendly(),
                nip05 = profileData.internetIdentifier,
                avatarUrl = profileData.picture,
                pubkey = profileData.ownerId
            )
        }
    }

    suspend fun muteUserAndPersistMutelist(userId: String, mutedUserPubkey: String) {
        updateAndPersistMutelist(userId = userId) {
            toMutableSet().apply {
                add(mutedUserPubkey)
            }
        }
    }

    suspend fun unmuteUserAndPersistMutelist(userId: String, unmutedUserPubkey: String) {
        updateAndPersistMutelist(userId = userId) {
            toMutableSet().apply {
                remove(unmutedUserPubkey)
            }
        }
    }

    private suspend fun updateAndPersistMutelist(
        userId: String,
        reducer: Set<String>.() -> Set<String>,
    ) {
        val remoteMutelist = fetchMutelist(userId = userId)
        val newMutelist = remoteMutelist.reducer()
        settingsApi.setMutelist(userId = userId, mutelist = newMutelist)
        persistMutelist(mutelist = newMutelist)
    }

    private suspend fun fetchMutelist(userId: String): Set<String> {
        val response = settingsApi.getMutelist(userId = userId)

        return response.mutelist?.tags?.filter { it.size == 2 }?.map { it[1].toString() }?.toSet()
            ?: emptySet()
    }

    private suspend fun persistMutelist(mutelist: Set<String>) {
        database.withTransaction {
            val muted = mutelist.map { it.asMutedPO() }.toSet()

            database.muted().deleteAll()
            database.muted().upsertAll(data = muted)
        }
    }
}

private fun String.asMutedPO(): Muted = Muted(pubkey = this)
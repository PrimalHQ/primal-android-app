package net.primal.android.user.repository

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.relays.BOOTSTRAP_RELAYS
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.user.accounts.parseNip65Relays
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.Relay as RelayDO
import net.primal.android.user.domain.RelayKind
import net.primal.android.user.domain.mapToRelayPO

class RelayRepository @Inject constructor(
    private val primalDatabase: PrimalDatabase,
    private val usersApi: UsersApi,
    private val nostrPublisher: NostrPublisher,
) {
    fun observeUserRelays(userId: String) =
        primalDatabase.relays().observeRelays(userId)
            .map { relays -> relays.filter { it.kind == RelayKind.UserRelay } }

    fun findRelays(userId: String, kind: RelayKind) = primalDatabase.relays().findRelays(userId, kind)

    suspend fun bootstrapDefaultUserRelays(userId: String) {
        nostrPublisher.publishRelayList(userId, BOOTSTRAP_RELAYS)
        replaceUserRelays(userId, BOOTSTRAP_RELAYS)
    }

    private suspend fun fetchUserRelays(userId: String): List<RelayDO>? {
        val response = usersApi.getUserRelays(userId)
        return response.cachedRelayListEvent?.tags?.parseNip65Relays()
    }

    suspend fun fetchAndUpdateUserRelays(userId: String) {
        val relayList = fetchUserRelays(userId)
        if (!relayList.isNullOrEmpty()) replaceUserRelays(userId, relayList)
    }

    private suspend fun replaceUserRelays(userId: String, relays: List<RelayDO>) {
        primalDatabase.withTransaction {
            primalDatabase.relays().deleteAll(userId = userId, kind = RelayKind.UserRelay)
            primalDatabase.relays().upsertAll(
                relays = relays.map {
                    it.mapToRelayPO(userId = userId, kind = RelayKind.UserRelay)
                },
            )
        }
    }

    suspend fun addRelayAndPublishRelayList(userId: String, url: String) {
        val newRelay = RelayDO(url = url, read = true, write = true)
        updateRelayList(userId = userId) {
            this.toMutableList().apply {
                add(0, newRelay)
            }
        }
    }

    suspend fun removeRelayAndPublishRelayList(userId: String, url: String) {
        updateRelayList(userId = userId) {
            this.toMutableList().apply {
                removeIf { it.url == url }
            }
        }
    }

    private suspend fun updateRelayList(userId: String, reducer: List<RelayDO>.() -> List<RelayDO>) {
        val latestRelayList = fetchUserRelays(userId = userId) ?: emptyList()
        val newRelayList = latestRelayList.reducer()
        nostrPublisher.publishRelayList(userId = userId, relays = newRelayList)
        replaceUserRelays(userId = userId, relays = newRelayList)
    }
}

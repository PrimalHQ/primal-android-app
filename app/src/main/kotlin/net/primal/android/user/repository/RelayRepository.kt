package net.primal.android.user.repository

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.relays.FALLBACK_RELAYS
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.notary.MissingPrivateKeyException
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.user.accounts.parseNip65Relays
import net.primal.android.user.domain.Relay as RelayDO
import net.primal.android.user.domain.RelayKind
import net.primal.android.user.domain.UserRelays
import net.primal.android.user.domain.cleanWebSocketUrl
import net.primal.android.user.domain.mapToRelayPO
import net.primal.android.user.domain.toRelay
import net.primal.core.networking.sockets.errors.WssException
import net.primal.data.remote.api.users.UsersApi
import timber.log.Timber

class RelayRepository @Inject constructor(
    private val primalDatabase: PrimalDatabase,
    private val usersApi: UsersApi,
    private val nostrPublisher: NostrPublisher,
    private val dispatchers: CoroutineDispatcherProvider,
) {
    fun observeUserRelays(userId: String) =
        primalDatabase.relays().observeRelays(userId)
            .map { relays -> relays.filter { it.kind == RelayKind.UserRelay } }

    fun findRelays(userId: String, kind: RelayKind) = primalDatabase.relays().findRelays(userId, kind)

    @Throws(NostrPublishException::class, MissingPrivateKeyException::class)
    suspend fun bootstrapUserRelays(userId: String) =
        withContext(dispatchers.io()) {
            val relays = try {
                usersApi.getDefaultRelays().map { it.toRelay() }
            } catch (error: WssException) {
                Timber.w(error)
                FALLBACK_RELAYS
            }
            replaceUserRelays(userId, relays)
            nostrPublisher.publishRelayList(userId, relays)
        }

    private suspend fun fetchUserRelays(userId: String): List<RelayDO>? {
        val response = withContext(dispatchers.io()) { usersApi.getUserRelays(listOf(userId)) }
        val cachedNip65Event = response.cachedRelayListEvents.firstOrNull() ?: return null
        return cachedNip65Event.tags.parseNip65Relays()
    }

    suspend fun fetchAndUpdateUserRelays(userId: String) {
        val relayList = fetchUserRelays(userId)
        if (relayList != null) replaceUserRelays(userId, relayList)
    }

    private suspend fun fetchUserRelays(userIds: List<String>) =
        withContext(dispatchers.io()) {
            usersApi.getUserRelays(userIds).cachedRelayListEvents
                .filterNot { it.pubKey == null }
                .map { UserRelays(pubkey = it.pubKey!!, relays = it.tags.parseNip65Relays()) }
        }

    suspend fun fetchAndUpdateUserRelays(userIds: List<String>): List<UserRelays> {
        return fetchUserRelays(userIds).onEach {
            replaceUserRelays(userId = it.pubkey, relays = it.relays)
        }
    }

    private suspend fun replaceUserRelays(userId: String, relays: List<RelayDO>) =
        withContext(dispatchers.io()) {
            primalDatabase.withTransaction {
                primalDatabase.relays().deleteAll(userId = userId, kind = RelayKind.UserRelay)
                primalDatabase.relays().upsertAll(
                    relays = relays.map {
                        it.mapToRelayPO(userId = userId, kind = RelayKind.UserRelay)
                    },
                )
            }
        }

    @Throws(NostrPublishException::class)
    suspend fun addRelayAndPublishRelayList(userId: String, url: String) {
        val newRelay = RelayDO(url = url, read = true, write = true)
        updateRelayList(userId = userId) {
            this.toMutableList().apply {
                add(0, newRelay)
            }
        }
    }

    @Throws(NostrPublishException::class)
    suspend fun removeRelayAndPublishRelayList(userId: String, url: String) {
        updateRelayList(userId = userId) {
            this.toMutableList().apply {
                removeIf { it.url == url.cleanWebSocketUrl() }
            }
        }
    }

    private suspend fun updateRelayList(userId: String, reducer: List<RelayDO>.() -> List<RelayDO>) =
        withContext(dispatchers.io()) {
            val latestRelayList = fetchUserRelays(userId = userId) ?: emptyList()
            val newRelayList = latestRelayList.reducer()
            nostrPublisher.publishRelayList(userId = userId, relays = newRelayList)
            replaceUserRelays(userId = userId, relays = newRelayList)
        }
}

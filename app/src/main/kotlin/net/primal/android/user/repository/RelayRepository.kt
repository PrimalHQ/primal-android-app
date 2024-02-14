package net.primal.android.user.repository

import androidx.room.withTransaction
import javax.inject.Inject
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.relays.BOOTSTRAP_RELAYS
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.user.accounts.parseKind3Relays
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
    fun findRelays(userId: String, kind: RelayKind) = primalDatabase.relays().findRelays(userId, kind)

    suspend fun bootstrapDefaultUserRelays(userId: String) {
        nostrPublisher.publishRelayList(userId, BOOTSTRAP_RELAYS)
        replaceUserRelays(userId, BOOTSTRAP_RELAYS)
    }

    suspend fun fetchAndUpdateUserRelays(userId: String) {
        val response = usersApi.getUserRelays(userId)
        val relayList = response.relayListMetadataEvent?.tags.parseNip65Relays()
        val relaysBackup = response.followListEvent?.content?.parseKind3Relays()

        val relays = relayList.ifEmpty { relaysBackup }
        if (!relays.isNullOrEmpty()) replaceUserRelays(userId, relays)
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
}

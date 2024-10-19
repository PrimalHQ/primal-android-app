package net.primal.android.stats.repository

import androidx.room.withTransaction
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsEventZapDO
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.stats.api.model.EventZapsResponse

suspend fun EventZapsResponse.persistToDatabaseAsTransaction(database: PrimalDatabase) {
    val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
    val profiles = this.profiles.mapAsProfileDataPO(cdnResources = cdnResources)
    val eventZaps = this.zaps.mapAsEventZapDO(profilesMap = profiles.associateBy { it.ownerId })
    database.withTransaction {
        database.profiles().upsertAll(data = profiles)
        database.eventZaps().upsertAll(data = eventZaps)
    }
}

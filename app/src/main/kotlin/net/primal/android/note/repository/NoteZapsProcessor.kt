package net.primal.android.note.repository

import androidx.room.withTransaction
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsNoteZapDO
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.note.api.model.NoteZapsResponse

suspend fun NoteZapsResponse.persistToDatabaseAsTransaction(database: PrimalDatabase) {
    val noteZaps = this.zaps.mapAsNoteZapDO()
    val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
    val profiles = this.profiles.mapAsProfileDataPO(cdnResources = cdnResources)
    database.withTransaction {
        database.profiles().upsertAll(data = profiles)
        database.noteZaps().upsertAll(data = noteZaps)
    }
}

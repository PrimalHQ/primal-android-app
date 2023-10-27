package net.primal.android.messages.api.mediator

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.db.PrimalDatabase
import net.primal.android.messages.api.model.MessagesResponse
import net.primal.android.nostr.ext.flatMapNotNullAsMediaResourcePO
import net.primal.android.nostr.ext.mapAsMessageDataPO
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.user.credentials.CredentialsStore

suspend fun MessagesResponse.processAndSave(
    userId: String,
    database: PrimalDatabase,
    credentialsStore: CredentialsStore,
) {
    val profiles = profileMetadata.mapAsProfileDataPO()
    val mediaResources = mediaResources.flatMapNotNullAsMediaResourcePO()
    val messages = messages.mapAsMessageDataPO(
        userId = userId,
        nsec = credentialsStore.findOrThrow(npub = userId.hexToNpubHrp()).nsec
    )

    withContext(Dispatchers.IO) {
        database.withTransaction {
            database.profiles().upsertAll(data = profiles)
            database.mediaResources().upsertAll(data = mediaResources)
            database.messages().upsertAll(data = messages)
        }
    }
}

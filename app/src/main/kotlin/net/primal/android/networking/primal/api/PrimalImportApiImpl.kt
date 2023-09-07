package net.primal.android.networking.primal.api

import kotlinx.serialization.encodeToString
import net.primal.android.networking.primal.PrimalClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.primal.api.model.ImportRequestBody
import net.primal.android.networking.sockets.NostrIncomingMessage
import net.primal.android.serialization.NostrJson
import javax.inject.Inject
import javax.inject.Named

class PrimalImportApiImpl @Inject constructor(
    @Named("Api") private val primalClient: PrimalClient,
) : PrimalImportApi {

    override suspend fun importEvents(body: ImportRequestBody): Boolean {
        val result = primalClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.IMPORT_EVENTS,
                optionsJson = NostrJson.encodeToString(body)
            )
        )

        return when (result.terminationMessage) {
            is NostrIncomingMessage.EoseMessage -> true
            else -> false
        }
    }

}

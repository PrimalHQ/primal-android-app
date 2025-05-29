package net.primal.core.networking.nwc

import kotlinx.serialization.json.JsonObject

interface NwcApi {

    suspend fun zap()

    suspend fun getBalance(): Long

    suspend fun getTransactions(): List<JsonObject>
}

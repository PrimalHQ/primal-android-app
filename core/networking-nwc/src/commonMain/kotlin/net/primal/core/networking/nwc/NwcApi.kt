package net.primal.core.networking.nwc

import kotlinx.serialization.json.JsonObject

interface NwcApi {

    suspend fun getBalance(): NwcResult<Long>

    suspend fun getTransactions(): NwcResult<List<JsonObject>>
}

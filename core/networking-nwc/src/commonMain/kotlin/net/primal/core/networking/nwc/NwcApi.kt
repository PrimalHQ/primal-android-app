package net.primal.core.networking.nwc

import kotlinx.serialization.json.JsonObject

interface NwcApi {

    suspend fun getBalance(): NwcResult<Long>

    suspend fun getTransactions(): NwcResult<List<JsonObject>>

    suspend fun makeInvoice()

    suspend fun lookupInvoice()

    suspend fun getInfo()

    suspend fun payInvoice()

    suspend fun payKeysend()

}

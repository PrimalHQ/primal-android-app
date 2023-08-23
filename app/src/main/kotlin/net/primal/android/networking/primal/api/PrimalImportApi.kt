package net.primal.android.networking.primal.api

import net.primal.android.networking.primal.api.model.ImportRequestBody

interface PrimalImportApi {

    suspend fun importEvents(body: ImportRequestBody): Boolean

}

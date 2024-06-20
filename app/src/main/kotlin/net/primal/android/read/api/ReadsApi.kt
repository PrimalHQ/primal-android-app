package net.primal.android.read.api

import net.primal.android.read.api.model.BlogThreadRequestBody
import net.primal.android.read.api.model.BlogThreadResponse

interface ReadsApi {

    suspend fun getBlogThread(body: BlogThreadRequestBody): BlogThreadResponse
}

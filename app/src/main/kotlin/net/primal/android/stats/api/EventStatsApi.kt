package net.primal.android.stats.api

import net.primal.android.stats.api.model.EventActionsRequestBody
import net.primal.android.stats.api.model.EventActionsResponse
import net.primal.android.stats.api.model.EventZapsRequestBody
import net.primal.android.stats.api.model.EventZapsResponse

interface EventStatsApi {

    suspend fun getEventZaps(body: EventZapsRequestBody): EventZapsResponse

    suspend fun getEventActions(body: EventActionsRequestBody): EventActionsResponse
}

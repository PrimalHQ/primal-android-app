package net.primal.android.events.api

import net.primal.android.events.api.model.EventActionsRequestBody
import net.primal.android.events.api.model.EventActionsResponse
import net.primal.android.events.api.model.EventZapsRequestBody
import net.primal.android.events.api.model.EventZapsResponse

interface EventStatsApi {

    suspend fun getEventZaps(body: EventZapsRequestBody): EventZapsResponse

    suspend fun getEventActions(body: EventActionsRequestBody): EventActionsResponse
}

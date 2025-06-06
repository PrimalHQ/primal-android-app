package net.primal.data.remote.api.events

import net.primal.data.remote.api.events.model.EventActionsRequestBody
import net.primal.data.remote.api.events.model.EventActionsResponse
import net.primal.data.remote.api.events.model.EventZapsRequestBody
import net.primal.data.remote.api.events.model.EventZapsResponse

interface EventStatsApi {

    suspend fun getEventZaps(body: EventZapsRequestBody): EventZapsResponse

    suspend fun getEventActions(body: EventActionsRequestBody): EventActionsResponse
}

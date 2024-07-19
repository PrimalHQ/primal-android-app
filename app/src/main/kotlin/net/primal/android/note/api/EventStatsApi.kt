package net.primal.android.note.api

import net.primal.android.note.api.model.EventActionsRequestBody
import net.primal.android.note.api.model.EventActionsResponse
import net.primal.android.note.api.model.EventZapsRequestBody
import net.primal.android.note.api.model.EventZapsResponse

interface EventStatsApi {

    suspend fun getEventZaps(body: EventZapsRequestBody): EventZapsResponse

    suspend fun getEventActions(body: EventActionsRequestBody): EventActionsResponse
}

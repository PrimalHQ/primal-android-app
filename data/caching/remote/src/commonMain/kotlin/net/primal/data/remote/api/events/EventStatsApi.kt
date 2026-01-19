package net.primal.data.remote.api.events

import net.primal.core.utils.Result
import net.primal.data.remote.api.events.model.EventActionsRequestBody
import net.primal.data.remote.api.events.model.EventActionsResponse
import net.primal.data.remote.api.events.model.EventZapsRequestBody
import net.primal.data.remote.api.events.model.EventZapsResponse
import net.primal.data.remote.api.events.model.EventsNip46Response
import net.primal.data.remote.api.events.model.InvoicesToZapReceiptsResponse
import net.primal.data.remote.api.events.model.ReplaceableEventRequest
import net.primal.data.remote.api.events.model.ReplaceableEventResponse
import net.primal.data.remote.api.events.model.ReplaceableEventsRequest

interface EventStatsApi {

    suspend fun getEventZaps(body: EventZapsRequestBody): EventZapsResponse

    suspend fun getEventActions(body: EventActionsRequestBody): EventActionsResponse

    suspend fun getReplaceableEvent(body: ReplaceableEventRequest): Result<ReplaceableEventResponse>

    suspend fun getReplaceableEvents(body: ReplaceableEventsRequest): Result<ReplaceableEventResponse>

    suspend fun getZapReceipts(invoices: List<String>): Result<InvoicesToZapReceiptsResponse>

    suspend fun getNip46Events(eventIds: List<String>): Result<EventsNip46Response>
}

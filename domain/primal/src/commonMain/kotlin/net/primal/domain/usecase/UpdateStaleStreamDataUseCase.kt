package net.primal.domain.usecase

import net.primal.core.utils.Result
import net.primal.domain.events.EventRepository
import net.primal.domain.streams.StreamRepository

class UpdateStaleStreamDataUseCase(
    private val streamRepository: StreamRepository,
    private val eventRepository: EventRepository,
) {
    suspend fun invoke(): Result<Unit> {
        val staleStreamNaddrs = streamRepository.getStaleStreamNaddrs()
        if (staleStreamNaddrs.isEmpty()) return Result.success(Unit)

        return eventRepository.fetchReplaceableEvents(naddrs = staleStreamNaddrs)
    }
}
